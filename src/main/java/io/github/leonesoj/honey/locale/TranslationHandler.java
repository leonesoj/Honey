package io.github.leonesoj.honey.locale;

import io.github.leonesoj.honey.config.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class TranslationHandler {

  public static final Locale DEFAULT_LOCALE = Locale.US;
  public static final String[] BUNDLED_LOCALES = {"en_US"};

  private static final String TRANSLATIONS_DIR = "translations";

  private MiniMessageTranslationStore translationStore;
  private final Map<String, Map<String, Config>> configTranslations = new HashMap<>();

  private final JavaPlugin plugin;

  public TranslationHandler(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void loadTranslationStore() {
    if (translationStore != null) {
      GlobalTranslator.translator().removeSource(translationStore);
    }

    translationStore = MiniMessageTranslationStore.create(Key.key("honey", "main"));
    translationStore.defaultLocale(DEFAULT_LOCALE);

    File baseDir = new File(plugin.getDataFolder(), TRANSLATIONS_DIR);
    if (!baseDir.exists()) {
      baseDir.mkdirs();
    }

    seedDefaultIfEmpty(baseDir.toPath());
    loadAllBundles(baseDir.toPath());

    GlobalTranslator.translator().addSource(translationStore);
  }

  private void seedDefaultIfEmpty(Path baseDir) {
    if (!Files.exists(baseDir)) {
      try {
        Files.createDirectories(baseDir);
      } catch (IOException e) {
        plugin.getLogger()
            .log(Level.SEVERE, "Failed to create translations directory: " + baseDir, e);
        return;
      }
    }

    for (String locale : BUNDLED_LOCALES) {
      String resourcePath = TRANSLATIONS_DIR + "/" + locale + ".properties";
      copyFromJarIfPresent(resourcePath);
    }
  }


  public void registerTranslationConfigs() {
    loadTranslationConfigs("report", "gui");
    loadTranslationConfigs("report-viewer", "gui");
    loadTranslationConfigs("settings", "gui");
    loadTranslationConfigs("staff-items", "other");
  }

  private void loadTranslationConfigs(String baseName, String path) {
    File folder = new File(plugin.getDataFolder(), path + File.separator + baseName);
    if (!folder.exists() && !folder.mkdirs()) {
      plugin.getLogger()
          .warning("Could not create translation folder: " + folder.getAbsolutePath());
      return;
    }

    for (String locale : BUNDLED_LOCALES) {
      String resourcePath = path + "/" + baseName + "/" + baseName + "-" + locale + ".yml";
      copyFromJarIfPresent(resourcePath);
    }

    File[] translationFiles = folder.listFiles((dir, name) ->
        name.startsWith(baseName + "-") && name.endsWith(".yml")
    );

    if (translationFiles == null || translationFiles.length == 0) {
      plugin.getLogger().warning("No translations found for base: " + baseName);
      return;
    }

    Map<String, Config> localeMap = new HashMap<>();
    for (File file : translationFiles) {
      String fileName = file.getName();
      int dash = fileName.lastIndexOf('-');
      int dot = fileName.lastIndexOf(".yml");
      if (dash <= 0 || dot <= dash) {
        continue;
      }

      String lang = fileName.substring(dash + 1, dot);
      String resourcePath = path + "/" + baseName + "/";
      Config cfg = new Config(plugin, resourcePath, baseName + "-" + lang, false);
      localeMap.put(normalizeLocaleTag(lang), cfg);
    }

    configTranslations.put(baseName, localeMap);
  }

  public CompletableFuture<Void> reloadTranslationConfigs() {
    CompletableFuture<?>[] futures = configTranslations.values().stream()
        .flatMap(m -> m.values().stream())
        .map(Config::loadConfig)
        .toArray(CompletableFuture[]::new);

    return CompletableFuture.allOf(futures);
  }

  private void copyFromJarIfPresent(String resourcePath) {
    File out = new File(plugin.getDataFolder(), resourcePath);
    if (out.exists()) {
      return;
    }

    File parent = out.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }

    try {
      plugin.saveResource(resourcePath, false);
      plugin.getLogger().info("Seeded translation file: " + resourcePath);
    } catch (IllegalArgumentException e) {
      plugin.getLogger().log(Level.SEVERE, "Bundled resource not found: " + resourcePath, e);
    }
  }

  private void loadAllBundles(Path baseDir) {
    if (!Files.exists(baseDir)) {
      return;
    }

    try (Stream<Path> paths = Files.walk(baseDir)) {
      paths.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".properties"))
          .forEach(this::loadBundleFile);
    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to scan translations directory", e);
    }
  }

  private void loadBundleFile(Path filePath) {
    String fileName = filePath.getFileName().toString();
    int idx = fileName.lastIndexOf(".properties");
    if (idx <= 0) {
      plugin.getLogger().warning("Invalid translation file name: " + fileName);
      return;
    }

    String localeStr = fileName.substring(0, idx).replace('-', '_');
    Locale locale = Translator.parseLocale(localeStr);
    if (locale == null) {
      plugin.getLogger().warning("Invalid locale in translation file: " + fileName);
      return;
    }

    try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
      PropertyResourceBundle bundle = new PropertyResourceBundle(reader);
      translationStore.registerAll(locale, bundle, false);

      plugin.getLogger().info("Loaded translation: " + baseDirRelative(filePath) + " -> " + locale);
    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to load translation file: " + filePath, e);
    }
  }

  private String baseDirRelative(Path filePath) {
    Path base = new File(plugin.getDataFolder(), TRANSLATIONS_DIR).toPath();
    try {
      return base.relativize(filePath).toString().replace(File.separatorChar, '/');
    } catch (IllegalArgumentException ignored) {
      return filePath.toString();
    }
  }

  private String normalizeLocaleTag(String tag) {
    if (tag == null || tag.isBlank()) {
      return "";
    }
    String t = tag.replace('_', '-').trim();
    String[] parts = t.split("-");
    String language = parts[0].toLowerCase(Locale.ROOT);
    if (parts.length == 1) {
      return language;
    }
    String region = parts[1].toUpperCase(Locale.ROOT);
    return language + "-" + region;
  }

  public FileConfiguration findBestTranslation(String baseName, Locale requestedLocale) {
    Map<String, Config> localeMap = configTranslations.get(baseName);
    if (localeMap == null || localeMap.isEmpty()) {
      return null;
    }

    String language = requestedLocale.getLanguage().toLowerCase(Locale.ROOT);
    String country = requestedLocale.getCountry().toUpperCase(Locale.ROOT);
    String exactTag = !country.isEmpty() ? language + "_" + country : language;

    if (localeMap.containsKey(exactTag)) {
      return localeMap.get(exactTag).getRawConfig();
    }

    if (localeMap.containsKey(language)) {
      return localeMap.get(language).getRawConfig();
    }

    if (localeMap.containsKey("en")) {
      return localeMap.get("en").getRawConfig();
    }

    return localeMap.values().stream().findFirst().get().getRawConfig();
  }
}

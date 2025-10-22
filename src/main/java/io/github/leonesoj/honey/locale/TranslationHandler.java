package io.github.leonesoj.honey.locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.stream.Stream;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.plugin.java.JavaPlugin;

public final class TranslationHandler {

  public static final Locale DEFAULT_LOCALE = Locale.US;
  private static final String TRANSLATIONS_DIR = "translations";

  private MiniMessageTranslationStore translationStore;

  private final JavaPlugin plugin;

  public TranslationHandler(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void load() {
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
    try (Stream<Path> s = Files.list(baseDir)) {
      boolean empty = s.findAny().isEmpty();
      if (!empty) {
        return;
      }
    } catch (IOException ignored) {
    }

    copyFromJarIfPresent("translations/en_US.properties");
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
}

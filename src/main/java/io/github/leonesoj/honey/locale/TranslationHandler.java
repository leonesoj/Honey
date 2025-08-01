package io.github.leonesoj.honey.locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.plugin.java.JavaPlugin;

public class TranslationHandler {

  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  private MiniMessageTranslationStore translationStore;

  private final JavaPlugin plugin;

  public TranslationHandler(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void load() {
    if (translationStore != null) {
      GlobalTranslator.translator().removeSource(translationStore);
    }

    translationStore = MiniMessageTranslationStore.create(
        Key.key("honey", "main"));
    translationStore.defaultLocale(DEFAULT_LOCALE);

    loadNativeBundles();
    loadCustomBundles();
    GlobalTranslator.translator().addSource(translationStore);
  }

  private void loadNativeBundles() {
    loadBundle("en_US", DEFAULT_LOCALE, true);
  }

  private void loadCustomBundles() {
    File customDir = new File(plugin.getDataFolder(), "translations/custom");

    if (!customDir.exists() || !customDir.isDirectory()) {
      customDir.mkdirs();
      return;
    }

    File[] files = customDir.listFiles((dir, name) -> name.endsWith(".properties"));
    if (files == null) {
      return;
    }

    for (File file : files) {
      String fileName = file.getName();
      int extensionIdx = fileName.lastIndexOf(".properties");
      if (extensionIdx == -1) {
        plugin.getLogger().warning("Invalid file name: " + fileName);
        continue;
      }

      String localeStr = fileName.substring(0, extensionIdx);
      Locale locale = Translator.parseLocale(localeStr);
      if (locale == null) {
        plugin.getLogger().warning("Found invalid locale for custom translation file: " + fileName);
      }

      loadBundle(localeStr, locale, false);
    }
  }


  private void loadBundle(String resourceName, Locale locale, boolean fromJar) {
    String folderName = fromJar ? "native" : "custom";
    String resourcePath = "translations/%s/%s.properties".formatted(folderName, resourceName);
    File file = new File(plugin.getDataFolder(), resourcePath);

    if (!file.exists() && fromJar) {
      plugin.saveResource(resourcePath, true);
    }

    try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      PropertyResourceBundle bundle = new PropertyResourceBundle(reader);
      translationStore.registerAll(locale, bundle, false);
      plugin.getLogger()
          .info("Successfully loaded %s translation file: %s".formatted(folderName, resourceName));
    } catch (IOException exception) {
      plugin.getLogger()
          .log(Level.SEVERE, "Failed to load translation file: " + resourceName, exception);
    }
  }

}

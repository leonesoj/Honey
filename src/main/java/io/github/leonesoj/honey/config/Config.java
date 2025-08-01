package io.github.leonesoj.honey.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {

  private final JavaPlugin plugin;

  private YamlConfiguration config;
  private final YamlConfiguration defaultConfig;

  private final File configFile;

  public Config(JavaPlugin plugin, String path, String name, boolean hasConfigUpdates) {
    this.plugin = plugin;
    String configName = name + ".yml";
    String configPath = path + configName;

    // Load default configuration values
    this.defaultConfig = YamlConfiguration.loadConfiguration(
        new InputStreamReader(plugin.getResource(configPath), StandardCharsets.UTF_8)
    );

    this.configFile = new File(plugin.getDataFolder(), configPath);

    plugin.getDataFolder().mkdirs();
    configFile.getParentFile().mkdirs();

    try {
      if (configFile.createNewFile()) {
        plugin.getLogger().info("Created config file: %s".formatted(configName));
        plugin.saveResource(configPath, true);
      }
    } catch (IOException exception) {
      plugin.getLogger().log(Level.SEVERE,
          "Failed to create config file: %s".formatted(configName),
          exception
      );
    }

    loadConfig();

    // if (hasConfigUpdates) {
    // updateConfig(path + name);
    // }
  }

  public void loadConfig() {
    Bukkit.getAsyncScheduler().runNow(plugin, task -> {
      try {
        config = YamlConfiguration.loadConfiguration(configFile);
      } catch (IllegalArgumentException exception) {
        plugin.getLogger().log(Level.SEVERE,
            "Failed to load config file: %s.yml".formatted(configFile.getName()),
            exception
        );
      }
    });
  }

  private void saveConfig() {
    Bukkit.getAsyncScheduler().runNow(plugin, task -> {
      try {
        config.save(configFile);
      } catch (IOException exception) {
        plugin.getLogger().log(Level.SEVERE,
            "Failed to save config file: %s.yml".formatted(configFile.getName()),
            exception
        );
      }
    });
  }

  public void updateConfig(String name) {
    String defaultVersion = defaultConfig.getString("config_version");
    String currentVersion = config.getString("config_version");

    if (isVersionOlder(currentVersion, defaultVersion)) {
      plugin.getLogger().info("Updating config '%s' from version %s to %s..."
          .formatted(name, currentVersion, defaultVersion));

      Bukkit.getAsyncScheduler().runNow(plugin, task -> {
        // Back up old config if needed
        File backupFile = new File(configFile.getParent(), name + "-backup.yml");
        try {
          Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
          plugin.getLogger().log(Level.WARNING,
              "Failed to backup old config file: %s.yml".formatted(backupFile.getName()),
              exception
          );
        }

        // Overwrite with default resource to preserve comments
        plugin.saveResource(name + ".yml", true);

        // Reload and merge old values
        YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(configFile);
        config.getValues(true).forEach((key, value) -> {
          if (!key.equals("config_version")) {
            newConfig.set(key, value);
          }
        });

        try {
          newConfig.save(configFile);
          config = newConfig;
          plugin.getLogger().info("Successfully updated config: %s.yml".formatted(name));
        } catch (IOException exception) {
          plugin.getLogger().log(Level.SEVERE,
              "Failed to save updated config: %s.yml".formatted(name),
              exception
          );
        }
      });
    }
  }

  public FileConfiguration getRawConfig() {
    return config;
  }

  public void set(String path, Object value) {
    config.set(path, value);
  }

  public Object get(String path) {
    return config.get(path, defaultConfig.get(path));
  }

  public String getString(String path) {
    return config.getString(path, defaultConfig.getString(path));
  }

  public boolean getBoolean(String path) {
    return config.getBoolean(path, defaultConfig.getBoolean(path));
  }

  public int getInt(String path) {
    return config.getInt(path, defaultConfig.getInt(path));
  }

  private boolean isVersionOlder(String current, String target) {
    String[] cur = current.split("\\.");
    String[] tar = target.split("\\.");

    for (int i = 0; i < Math.max(cur.length, tar.length); i++) {
      int curVal = i < cur.length ? Integer.parseInt(cur[i]) : 0;
      int tarVal = i < tar.length ? Integer.parseInt(tar[i]) : 0;

      if (curVal < tarVal) {
        return true;
      }
      if (curVal > tarVal) {
        return false;
      }
    }
    return false;
  }


}

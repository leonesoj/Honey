package io.github.leonesoj.honey.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigHandler {

  private final JavaPlugin plugin;

  private final Map<String, Config> configs = new HashMap<>();

  public ConfigHandler(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void create() {
    registerConfig("config", "", true);
    registerConfig("report-gui", "gui/", false);
    registerConfig("report-viewer-gui", "gui/", false);
    registerConfig("settings-gui", "gui/", false);
    registerConfig("staff-items", "other/", false);
  }

  private void registerConfig(String name, String path, boolean hasUpdates) {
    configs.put(name, new Config(plugin, path, name, hasUpdates));
  }

  public Config getMainConfig() {
    return configs.get("config");
  }

  public FileConfiguration getReportGui() {
    return configs.get("report-gui").getRawConfig();
  }

  public FileConfiguration getReportViewerGui() {
    return configs.get("report-viewer-gui").getRawConfig();
  }

  public FileConfiguration getSettingsGui() {
    return configs.get("settings-gui").getRawConfig();
  }

  public FileConfiguration getStaffItems() {
    return configs.get("staff-items").getRawConfig();
  }

  public CompletableFuture<Void> reloadConfigs() {
    CompletableFuture<?>[] futures = configs.values().stream()
        .map(Config::loadConfig)
        .toArray(CompletableFuture[]::new);

    return CompletableFuture.allOf(futures);
  }

}

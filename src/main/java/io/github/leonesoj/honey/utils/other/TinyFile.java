package io.github.leonesoj.honey.utils.other;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class TinyFile {

  private final JavaPlugin plugin;

  private final File file;
  private YamlConfiguration config;

  public TinyFile(JavaPlugin plugin, String fileName, String path) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(),
        (path.isEmpty() ? "" : path + File.separator) + fileName + ".yml");
  }

  public CompletableFuture<YamlConfiguration> loadConfig() {
    CompletableFuture<YamlConfiguration> future = new CompletableFuture<>();
    Bukkit.getAsyncScheduler().runNow(plugin, task -> {
      try {
        if (file.getParentFile() != null) {
          file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
          file.createNewFile();
        }
        config = YamlConfiguration.loadConfiguration(file);
        future.complete(config);
      } catch (IOException exception) {
        future.completeExceptionally(exception);
      }
    });
    return future;
  }

  public CompletableFuture<Void> editAndSave(Consumer<YamlConfiguration> editor) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    loadConfig().thenAccept(cfg -> {
      try {
        editor.accept(cfg);
        cfg.save(file);
        future.complete(null);
      } catch (Exception e) {
        plugin.getLogger().severe("Failed to save: " + e.getMessage());
        future.completeExceptionally(e);
      }
    }).exceptionally(ex -> {
      future.completeExceptionally(ex);
      return null;
    });

    return future;
  }

  public boolean exists() {
    return file.exists();
  }

  public void deleteFile() {
    Bukkit.getAsyncScheduler().runNow(plugin, task -> {
      if (file.exists()) {
        file.delete();
      }
    });
  }
}

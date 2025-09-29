package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.data.model.PlayerSettings;
import io.github.leonesoj.honey.database.providers.DataStore;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SettingsController extends DataController<PlayerSettings> implements Listener {

  private static final long TIMEOUT_MS = 1500;

  public SettingsController(DataStore data, CacheStore nearCache, CacheStore sharedCache) {
    super(data,
        new DataContainer(
            PlayerSettings.STORAGE_KEY,
            PlayerSettings.PRIMARY_KEY,
            PlayerSettings.SCHEMA,
            Collections.emptySet()
        ),
        nearCache,
        sharedCache,
        PlayerSettings::deserialize,
        PlayerSettings::deserializeFromJson,
        Honey.getInstance()
    );
    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
  }

  public Optional<PlayerSettings> getSettings(UUID uuid) {
    String key = buildKey(uuid);
    return nearCache.get(key, PlayerSettings::deserializeFromJson).getNow(Optional.empty());
  }

  public CompletableFuture<Boolean> updateSettingsSync(PlayerSettings settings) {
    return update(settings.getUniqueId(), settings)
        .thenApply(result -> result)
        .thenCompose(this::completeOnMainThread);
  }

  private PlayerSettings defaultSettings(UUID uuid) {
    return new PlayerSettings(
        uuid,
        true,
        true,
        true,
        true,
        java.util.Collections.emptySet()
    );
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPreJoin(AsyncPlayerPreLoginEvent event) {
    UUID uuid = event.getUniqueId();

    Optional<PlayerSettings> loaded = get(uuid)
        .orTimeout(TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
        .exceptionally(ex -> {
          getLogger().warning(
              "(PlayerSettings) load failed for " + uuid + ": " + ex.getMessage()
          );
          return Optional.empty();
        })
        .join();

    PlayerSettings settings = loaded.orElseGet(() -> defaultSettings(uuid));

    if (loaded.isEmpty()) {
      create(uuid, settings)
          .orTimeout(TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
          .whenComplete((ok, ex) -> {
            boolean created = ex == null && Boolean.TRUE.equals(ok);
            if (!created) {
              String key = buildKey(uuid);
              nearCache.put(key, settings)
                  .exceptionally(e -> {
                    getLogger().warning(
                        "(PlayerSettings) nearCache.put fallback failed for " + uuid + ": "
                            + e.getMessage()
                    );
                    return false;
                  });
            }
          });
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    evictKey(event.getPlayer().getUniqueId());
  }
}

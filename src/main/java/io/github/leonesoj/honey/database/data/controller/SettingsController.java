package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.cache.InMemoryCache;
import io.github.leonesoj.honey.database.cache.NoOpCache;
import io.github.leonesoj.honey.database.cache.RedisCache;
import io.github.leonesoj.honey.database.data.model.PlayerSettings;
import io.github.leonesoj.honey.database.providers.DataStore;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class SettingsController extends DataController<PlayerSettings> implements Listener {

  private InMemoryCache hotCache;

  public SettingsController(DataStore data, CacheStore cache) {
    super(data, cache,
        new DataContainer(PlayerSettings.STORAGE_KEY,
            PlayerSettings.PRIMARY_KEY,
            PlayerSettings.SCHEMA,
            Collections.emptySet()
        ),
        PlayerSettings::deserialize,
        PlayerSettings::deserializeFromJson,
        Honey.getInstance(),
        true
    );

    if (cache instanceof RedisCache) {
      hotCache = new InMemoryCache(Honey.getInstance().getLogger());
    }

    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
  }

  public CompletableFuture<Optional<PlayerSettings>> getSettings(UUID uuid) {
    if (hotCache != null) {
      return hotCache.get(uuid.toString(), null)
          .thenCompose(dataModel -> {
            Optional<PlayerSettings> optional = dataModel.map(PlayerSettings.class::cast);
            if (optional.isPresent()) {
              return CompletableFuture.completedFuture(optional);
            }

            return get(uuid);
          });
    }

    return get(uuid);
  }

  public CompletableFuture<Optional<PlayerSettings>> getSettingsSync(UUID uuid) {
    return getSettings(uuid).thenCompose(this::completeOnMainThread);
  }

  public CompletableFuture<Boolean> updateSettingsSync(PlayerSettings settings) {
    return update(settings.getUniqueId(), settings)
        .thenApply(result -> {
          if (result && hotCache != null) {
            hotCache.put(settings.getUniqueId().toString(), settings);
          }

          return result;
        })
        .thenCompose(this::completeOnMainThread);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
    UUID uuid = event.getUniqueId();

    try {
      Optional<PlayerSettings> optional = getSettings(uuid).get();
      if (optional.isEmpty()) {
        PlayerSettings model = new PlayerSettings(
            uuid,
            true,
            true,
            true,
            true,
            Collections.emptySet()
        );

        boolean success = create(uuid, model).get();
        if (success && hotCache != null) {
          hotCache.put(uuid.toString(), model);
        }
      }

    } catch (ExecutionException | InterruptedException ignored) {
      event.disallow(Result.KICK_OTHER,
          Component.text("Failed to retrieve your player settings, please try again later",
              NamedTextColor.RED
          )
      );
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerLeave(PlayerQuitEvent event) {
    if (!(cache instanceof NoOpCache)) {
      keysToFlush.add(buildKey(event.getPlayer().getUniqueId()));
    }
  }

}

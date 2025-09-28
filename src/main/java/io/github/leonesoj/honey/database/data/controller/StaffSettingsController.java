package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.data.model.StaffSettings;
import io.github.leonesoj.honey.database.providers.DataStore;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffSettingsController extends DataController<StaffSettings> implements Listener {

  public StaffSettingsController(DataStore data, CacheStore nearCache, CacheStore sharedCache) {
    super(data,
        new DataContainer(
            StaffSettings.STORAGE_KEY,
            StaffSettings.PRIMARY_KEY,
            StaffSettings.SCHEMA,
            Collections.emptySet()
        ),
        nearCache,
        sharedCache,
        StaffSettings::deserialize,
        StaffSettings::deserializeFromJson,
        Honey.getInstance()
    );

    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
  }

  public CompletableFuture<Optional<StaffSettings>> getSettings(UUID uuid) {
    return get(uuid);
  }

  public CompletableFuture<Optional<StaffSettings>> getSettingsSync(UUID uuid) {
    return getSettings(uuid).thenCompose(this::completeOnMainThread);
  }

  public CompletableFuture<Boolean> createSettings(UUID uuid) {
    return create(uuid, defaultSettings(uuid));
  }

  public CompletableFuture<Boolean> deleteSettings(UUID uuid) {
    return delete(uuid);
  }

  public CompletableFuture<Boolean> modifySettings(UUID uuid,
      Function<StaffSettings, StaffSettings> mutator) {
    return getSettings(uuid).thenCompose(optional -> {
      if (optional.isPresent()) {
        StaffSettings mutated = mutator.apply(optional.get());
        return updateSettings(mutated);
      }
      return CompletableFuture.completedFuture(false);
    });
  }

  public CompletableFuture<Boolean> updateSettings(StaffSettings settings) {
    return update(settings.getUniqueId(), settings);
  }

  public CompletableFuture<Boolean> updateSettingsSync(StaffSettings settings) {
    return updateSettings(settings).thenCompose(this::completeOnMainThread);
  }

  private StaffSettings defaultSettings(UUID uuid) {
    return new StaffSettings(
        uuid,
        false,
        true,
        false,
        true,
        false,
        true
    );
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPreJoin(AsyncPlayerPreLoginEvent event) {
    UUID uuid = event.getUniqueId();
    LuckPerms lp = LuckPermsProvider.get();

    lp.getUserManager().loadUser(uuid)
        .orTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
        .thenCompose(user -> {
          if (user == null) {
            return CompletableFuture.completedFuture(false);
          }

          boolean allowed = user.getCachedData()
              .getPermissionData()
              .checkPermission("honey.management.staff")
              .asBoolean();

          if (!allowed) {
            return CompletableFuture.completedFuture(false);
          }

          return get(uuid).thenCompose(opt -> {
            if (opt.isPresent()) {
              return CompletableFuture.completedFuture(true);
            }
            return nearCache.put(buildKey(uuid), defaultSettings(uuid))
                .exceptionally(throwable -> true)
                .thenApply(ok -> true);
          });
        })
        .exceptionally(ex -> {
          nearCache.put(buildKey(uuid), defaultSettings(uuid)).exceptionally(throwable -> true);
          return false;
        });
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    evictKey(event.getPlayer().getUniqueId());
  }

}

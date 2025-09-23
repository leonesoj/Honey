package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.cache.InMemoryCache;
import io.github.leonesoj.honey.database.cache.RedisCache;
import io.github.leonesoj.honey.database.data.model.StaffSettings;
import io.github.leonesoj.honey.database.providers.DataStore;
import io.github.leonesoj.honey.utils.other.DependCheck;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class StaffSettingsController extends DataController<StaffSettings> implements Listener {

  private InMemoryCache hotCache;

  public StaffSettingsController(DataStore data, CacheStore cache) {
    super(data, cache,
        new DataContainer(
            StaffSettings.STORAGE_KEY,
            StaffSettings.PRIMARY_KEY,
            StaffSettings.SCHEMA,
            Collections.emptySet()
        ),
        StaffSettings::deserialize,
        StaffSettings::deserializeFromJson,
        Honey.getInstance(),
        false
    );

    if (cache instanceof RedisCache) {
      hotCache = new InMemoryCache(Honey.getInstance().getLogger());
    }

    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
  }

  public CompletableFuture<Optional<StaffSettings>> getSettings(UUID uuid) {
    if (hotCache != null) {
      return hotCache.get(uuid.toString(), null)
          .thenCompose(dataModel -> {
            Optional<StaffSettings> optional = dataModel.map(StaffSettings.class::cast);
            if (optional.isPresent()) {
              return CompletableFuture.completedFuture(optional);
            }

            return get(uuid);
          });
    }

    return get(uuid);
  }

  public CompletableFuture<Optional<StaffSettings>> getSettingsSync(UUID uuid) {
    return getSettings(uuid).thenCompose(this::completeOnMainThread);
  }

  public CompletableFuture<Boolean> createSettings(UUID uuid) {
    StaffSettings defaultSettings = defaultSettings(uuid);
    CompletableFuture<Boolean> databaseCreate = create(uuid, defaultSettings);
    return hotCache != null ? hotCache.put(uuid.toString(), defaultSettings) : databaseCreate;
  }

  public CompletableFuture<Boolean> deleteSettings(UUID uuid) {
    CompletableFuture<Boolean> databaseDelete = delete(uuid);
    return hotCache != null ? hotCache.delete(uuid.toString()) : databaseDelete;
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
    return update(settings.getUniqueId(), settings)
        .thenApply(result -> {
          if (result && hotCache != null) {
            hotCache.put(settings.getUniqueId().toString(), settings);
          }

          return result;
        });
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

  @EventHandler
  public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
    UUID uuid = event.getUniqueId();

    if (hasPermissionOfflineWithVault(uuid)) {
      try {
        Optional<StaffSettings> optional = get(uuid).get();
        StaffSettings cachedEntry;
        if (optional.isEmpty()) {
          create(uuid, defaultSettings(uuid)).get();
          cachedEntry = defaultSettings(uuid);
        } else {
          cachedEntry = optional.get();
        }

        if (hotCache != null) {
          hotCache.put(uuid.toString(), cachedEntry);
        }
      } catch (InterruptedException | ExecutionException exception) {
        cache.put(uuid.toString(), defaultSettings(uuid));
        if (hotCache != null) {
          hotCache.put(uuid.toString(), defaultSettings(uuid));
        }
      }
    }
  }

  private boolean hasPermissionOfflineWithVault(UUID uuid) {
    if (DependCheck.isVaultInstalled()) {
      RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager()
          .getRegistration(Permission.class);
      if (rsp != null) {
        Permission permissionProvider = rsp.getProvider();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return permissionProvider.playerHas(null, offlinePlayer, "honey.management.staff");
      }
    }
    return false;
  }

}

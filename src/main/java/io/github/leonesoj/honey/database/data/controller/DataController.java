package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.CacheProvider;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.cache.NoOpCache;
import io.github.leonesoj.honey.database.data.model.DataModel;
import io.github.leonesoj.honey.database.providers.DataStore;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.observer.EventType;
import io.github.leonesoj.honey.observer.ObserverService;
import io.lettuce.core.json.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class DataController<T extends DataModel> implements Listener {

  private static final long SHARED_CACHE_TIMEOUT_MS = 100L;

  protected final DataStore data;
  protected final DataContainer container;

  protected final CacheStore nearCache;
  protected final CacheStore sharedCache;

  protected static final CacheStore NOOP_SHARED_INSTANCE = new NoOpCache();

  private final Function<DataRecord, T> recordDeserializer;
  private final Function<JsonObject, T> jsonDeserializer;

  private final ObserverService<T> observerService;
  private final JavaPlugin plugin;

  public DataController(DataStore data,
      DataContainer container,
      CacheStore nearCache,
      CacheStore sharedCache,
      Function<DataRecord, T> recordDeserializer,
      Function<JsonObject, T> jsonDeserializer,
      JavaPlugin plugin) {
    this.data = data;
    this.container = container;
    this.data.createDataStore(container);

    this.nearCache = nearCache;
    this.sharedCache = sharedCache;

    this.recordDeserializer = recordDeserializer;
    this.jsonDeserializer = jsonDeserializer;

    this.observerService = new ObserverService<>();
    this.plugin = plugin;
  }

  protected CompletableFuture<Optional<T>> get(UUID uuid, String index) {
    final String key = buildKey(uuid);

    Optional<T> nearHit = nearCache.get(key, jsonDeserializer).getNow(Optional.empty());
    if (nearHit.isPresent()) {
      return CompletableFuture.completedFuture(nearHit);
    }

    CompletableFuture<Optional<T>> fromShared = sharedEnabled()
        ? sharedCache.get(key, jsonDeserializer).exceptionally(ex -> Optional.empty())
        : CompletableFuture.completedFuture(Optional.empty());

    return fromShared.thenCompose(sharedOpt -> {
      if (sharedOpt.isPresent()) {
        T model = sharedOpt.get();

        return nearCache.put(key, model)
            .exceptionally(ex -> true)
            .thenApply(ok -> sharedOpt);
      }

      return data.query(container, index, uuid, recordDeserializer)
          .thenCompose(dbOpt -> {
            if (dbOpt.isEmpty()) {
              return CompletableFuture.completedFuture(Optional.empty());
            }
            T model = dbOpt.get();

            CompletableFuture<Boolean> nearPut =
                nearCache.put(key, model).exceptionally(ex -> true);

            if (sharedEnabled()) {
              bestEffort(sharedCache.put(key, model), "shared.put-after-db:" + key);
            }

            return nearPut.thenApply(ok -> Optional.of(model));
          });
    });
  }

  protected CompletableFuture<Optional<T>> get(UUID uuid) {
    return get(uuid, container.primaryIndex());
  }

  protected CompletableFuture<List<T>> getMany(String index, Object value, int limit, int offset) {
    return data.queryMany(container, index, value, limit, offset, recordDeserializer);
  }

  protected CompletableFuture<Boolean> create(UUID uuid, T model) {
    String key = buildKey(uuid);

    return data.insert(container, model.serialize())
        .thenCompose(ok -> {
          if (!ok) {
            return CompletableFuture.completedFuture(false);
          }

          CompletableFuture<Boolean> nearPut =
              nearCache.put(key, model).exceptionally(ex -> true);

          if (sharedEnabled()) {
            bestEffort(sharedCache.put(key, model), "shared.put-create:" + key);
          }

          return nearPut.thenApply(result -> true);
        })
        .whenComplete((ok, ex) -> {
          if (ex == null && Boolean.TRUE.equals(ok)) {
            publishAsync(model, EventType.CREATE);
          }
        });
  }

  protected CompletableFuture<Boolean> update(UUID uuid, String index, T model) {
    String key = buildKey(uuid);

    return data.update(container, index, uuid, model.serialize())
        .thenCompose(ok -> {
          if (!ok) {
            return CompletableFuture.completedFuture(false);
          }

          CompletableFuture<Boolean> nearPut =
              nearCache.put(key, model).exceptionally(ex -> true);

          if (sharedEnabled()) {
            bestEffort(sharedCache.delete(key), "shared.invalidate-update:" + key);
          }

          return nearPut.thenApply(result -> true);
        })
        .whenComplete((ok, ex) -> {
          if (ex == null && Boolean.TRUE.equals(ok)) {
            publishAsync(model, EventType.UPDATE);
          }
        });
  }

  protected CompletableFuture<Boolean> update(UUID uuid, T model) {
    return update(uuid, container.primaryIndex(), model);
  }

  protected CompletableFuture<Boolean> delete(UUID uuid) {
    String key = buildKey(uuid);

    return data.delete(container, container.primaryIndex(), uuid)
        .thenCompose(ok -> {
          if (!ok) {
            return CompletableFuture.completedFuture(false);
          }

          CompletableFuture<Boolean> nearDel =
              nearCache.delete(key).exceptionally(ex -> true);

          if (sharedEnabled()) {
            bestEffort(sharedCache.delete(key), "shared.invalidate-delete:" + key);
          }

          return nearDel.thenApply(result -> true);
        })
        .whenComplete((ok, ex) -> {
          if (ex == null && Boolean.TRUE.equals(ok)) {
            publishAsync(null, EventType.DELETE);
          }
        });
  }

  private <U> CompletableFuture<U> bestEffort(CompletableFuture<U> future, String what) {
    return future.orTimeout(SHARED_CACHE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .exceptionally(ex -> {
          getLogger().warning(() -> "{shared cache}[" + what + "] " + ex.getMessage());
          return null;
        });
  }

  protected void evictKey(UUID uuid) {
    String key = buildKey(uuid);
    nearCache.delete(key).exceptionally(ex -> true);
  }

  protected String buildKey(UUID uuid) {
    return container.containerName() + ":" + uuid;
  }

  private boolean sharedEnabled() {
    return sharedCache != null && sharedCache.getProvider() != CacheProvider.NO_OP;
  }

  protected <U> CompletableFuture<U> completeOnMainThread(U value) {
    CompletableFuture<U> future = new CompletableFuture<>();
    Bukkit.getGlobalRegionScheduler().run(plugin, task -> future.complete(value));
    return future;
  }

  private void publishAsync(T model, EventType type) {
    Bukkit.getGlobalRegionScheduler()
        .run(plugin, task -> observerService.publishEvent(model, type));
  }

  public ObserverService<T> getObserverService() {
    return observerService;
  }

  public Logger getLogger() {
    return plugin.getLogger();
  }
}

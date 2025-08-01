package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.data.model.DataModel;
import io.github.leonesoj.honey.database.providers.DataStore;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.observer.EventType;
import io.github.leonesoj.honey.observer.ObserverService;
import io.lettuce.core.json.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

// TODO: Refactor DataStore and this class to support different indexes: i.e get, update

public abstract class DataController<T extends DataModel> implements Listener {

  protected final DataStore data;
  protected final DataContainer container;

  protected final CacheStore cache;
  protected final Set<String> keysToFlush = new HashSet<>();

  private final Function<DataRecord, T> recordDeserializer;
  private final Function<JsonObject, T> jsonDeserializer;

  private final ObserverService<T> observerService;

  private final JavaPlugin plugin;

  public DataController(DataStore data, CacheStore cache, DataContainer container,
      Function<DataRecord, T> recordDeserializer, Function<JsonObject, T> jsonDeserializer,
      JavaPlugin plugin, boolean enableFlush) {
    this.data = data;
    this.cache = cache;

    this.recordDeserializer = recordDeserializer;
    this.jsonDeserializer = jsonDeserializer;

    this.container = container;
    this.data.createDataStore(container);

    this.plugin = plugin;

    this.observerService = new ObserverService<>();

    if (enableFlush) {
      scheduleCacheFlush();
    }
  }

  protected CompletableFuture<Optional<T>> get(UUID uuid, String index) {
    return cache.get(buildKey(uuid), jsonDeserializer)
        .exceptionally(throwable -> Optional.empty())
        .thenCompose(cachedResult -> {
          if (cachedResult.isPresent()) {
            return completeOnMainThread(cachedResult);
          }

          return data.query(container, index, uuid.toString(), recordDeserializer)
              .thenApply(optional -> {
                optional.ifPresent(value -> cache.put(buildKey(uuid), value));
                return optional;
              });
        });
  }

  protected CompletableFuture<Optional<T>> get(UUID uuid) {
    return get(uuid, container.primaryIndex());
  }

  protected CompletableFuture<List<T>> getMany(String index, String value, int limit, int offset) {
    return data.queryMany(container, index, value, limit, offset, recordDeserializer)
        .thenCompose(list -> {
          CompletableFuture<List<T>> future = new CompletableFuture<>();
          Bukkit.getGlobalRegionScheduler().run(plugin, task -> future.complete(list));
          return future;
        });
  }

  protected CompletableFuture<Boolean> create(UUID uuid, T model) {
    return data.insert(container, model.serialize())
        .thenApply(result -> {
          cache.put(buildKey(uuid), model);
          Bukkit.getGlobalRegionScheduler().run(plugin,
              task -> observerService.publishEvent(model, EventType.CREATE)
          );
          return result;
        });
  }

  protected CompletableFuture<Boolean> update(UUID uuid, String index, T model) {
    return data.update(container, index, uuid, model.serialize())
        .thenApply(result -> {
          cache.put(buildKey(uuid), model);
          Bukkit.getGlobalRegionScheduler().run(plugin,
              task -> observerService.publishEvent(model, EventType.UPDATE)
          );
          return result;
        });
  }

  protected CompletableFuture<Boolean> update(UUID uuid, T model) {
    return update(uuid, container.primaryIndex(), model);
  }

  protected CompletableFuture<Boolean> delete(UUID uuid) {
    return data.delete(container, container.primaryIndex(), uuid.toString())
        .thenApply(result -> {
          cache.delete(buildKey(uuid));
          Bukkit.getGlobalRegionScheduler().run(plugin,
              task -> observerService.publishEvent(null, EventType.DELETE)
          );
          return result;
        });
  }

  protected <U> CompletableFuture<U> completeOnMainThread(U value) {
    CompletableFuture<U> future = new CompletableFuture<>();
    Bukkit.getGlobalRegionScheduler().run(plugin, task -> future.complete(value));
    return future;
  }

  private void scheduleCacheFlush() {
    Bukkit.getGlobalRegionScheduler().runAtFixedRate(
        plugin,
        task -> {
          cache.flush(keysToFlush);
          keysToFlush.clear();
        },
        6_000,
        6_000
    );
  }

  public Logger getLogger() {
    return plugin.getLogger();
  }

  public ObserverService<T> getObserverService() {
    return observerService;
  }

  protected String buildKey(UUID uuid) {
    return uuid.toString() + ":" + container.containerName();
  }
}

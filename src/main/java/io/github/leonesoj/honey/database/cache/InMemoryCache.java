package io.github.leonesoj.honey.database.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import io.github.leonesoj.honey.database.data.model.DataModel;
import io.lettuce.core.json.JsonObject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class InMemoryCache extends CacheStore {

  private final Cache<String, DataModel> cache;

  public InMemoryCache(Logger log) {
    super(log, CacheProvider.IN_MEMORY);
    this.cache = Caffeine.newBuilder()
        .maximumSize(250)
        .recordStats()
        .build();

    logInitialize();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends DataModel> CompletableFuture<Optional<T>> get(String key,
      Function<JsonObject, T> unused) {
    DataModel result = cache.getIfPresent(key);
    try {
      return CompletableFuture.completedFuture(Optional.ofNullable((T) result));
    } catch (ClassCastException e) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Cached value type mismatch", e));
    }
  }

  @Override
  public CompletableFuture<Boolean> put(String key, DataModel value) {
    cache.put(key, value);
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public CompletableFuture<Boolean> delete(String key) {
    cache.invalidate(key);
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public CompletableFuture<Boolean> flush(Set<String> keys) {
    if (!keys.isEmpty()) {
      cache.invalidateAll(keys);
      logFlush(keys.size());
    }
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public double getHitRate() {
    return cache.stats().evictionWeight();
  }

  @Override
  public void shutdownCache() {
    cache.cleanUp();
    logShutdown();
  }
}

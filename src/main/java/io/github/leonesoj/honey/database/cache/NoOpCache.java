package io.github.leonesoj.honey.database.cache;

import io.github.leonesoj.honey.database.data.model.DataModel;
import io.lettuce.core.json.JsonObject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NoOpCache extends CacheStore {

  public NoOpCache() {
    super(null, null);
  }

  @Override
  public <T extends DataModel> CompletableFuture<Optional<T>> get(String key,
      Function<JsonObject, T> jsonDeserializer) {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<Boolean> put(String key, DataModel value) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Boolean> delete(String key) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Boolean> flush(Set<String> keys) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public double getHitRate() {
    return 0.0;
  }

  @Override
  public void shutdownCache() {
    // no-op
  }
}

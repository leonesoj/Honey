package io.github.leonesoj.honey.database.cache;

import io.github.leonesoj.honey.database.data.model.DataModel;
import io.lettuce.core.json.JsonObject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CacheStore {

  private final Logger logger;

  private final CacheProvider provider;

  public CacheStore(Logger logger, CacheProvider provider) {
    this.logger = logger;
    this.provider = provider;
  }

  protected Logger getLogger() {
    return logger;
  }

  public abstract <T extends DataModel> CompletableFuture<Optional<T>> get(String key,
      Function<JsonObject, T> deserializer);

  public abstract CompletableFuture<Boolean> put(String key, DataModel value);

  public abstract CompletableFuture<Boolean> delete(String key);

  public abstract CompletableFuture<Boolean> flush(Set<String> keys);

  public abstract double getHitRate();

  public abstract void shutdownCache();

  public CacheProvider getProvider() {
    return provider;
  }

  protected void logInitialize() {
    logger.info("%s Successfully initialized cache store".formatted(getLogPrefix()));
  }

  protected void logShutdown() {
    logger.info("%s Successfully shutdown cache store".formatted(getLogPrefix()));
  }

  protected void logGetError(String key, Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s GET operation failed (key: %s)"
            .formatted(getLogPrefix(), key),
        throwable
    );
  }

  protected void logPutError(String key, Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s PUT operation failed (key: %s)"
            .formatted(getLogPrefix(), key),
        throwable
    );
  }

  protected void logDeleteError(String key, Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s DELETE operation failed (key: %s)".formatted(getLogPrefix(), key),
        throwable
    );
  }

  protected void logFlush(int numberOfKeys) {
    logger.log(Level.INFO,
        "%s Flushed %d keys from cache".formatted(getLogPrefix(), numberOfKeys)
    );
  }

  protected String getLogPrefix() {
    return "|%s - CACHE|".formatted(provider.toString());
  }
}

package io.github.leonesoj.honey.database.cache;

import io.github.leonesoj.honey.database.data.model.DataModel;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.json.DefaultJsonParser;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonPath;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisCache extends CacheStore {

  private final RedisClient redisClient;
  private final StatefulRedisConnection<String, String> connection;
  private final RedisAsyncCommands<String, String> commands;

  public RedisCache(String host, int port, String password, Logger logger) {
    super(logger, CacheProvider.REDIS);
    this.redisClient = RedisClient.create(RedisURI.Builder.redis(host, port)
        .withPassword(password.toCharArray())
        .withSsl(true).build());
    // Nowhere in the Lettuce documentation does it say you have to do this when using Jackson
    this.redisClient.setOptions(ClientOptions.builder().jsonParser(DefaultJsonParser::new).build());

    try {
      this.connection = redisClient.connect();
      this.commands = connection.async();
      logInitialize();
    } catch (RedisException exception) {
      String errorMessage = "%s Failed to connect to Redis Database".formatted(getLogPrefix());
      getLogger().log(Level.SEVERE,
          errorMessage,
          exception
      );
      throw exception;
    }
  }

  @Override
  public <T extends DataModel> CompletableFuture<Optional<T>> get(String key,
      Function<JsonObject, T> deserializer) {
    return commands.jsonGet(key, JsonPath.ROOT_PATH)
        .toCompletableFuture()
        .exceptionally(throwable -> {
          logGetError(key, throwable);
          throw new CompletionException(throwable);
        })
        .thenApply(jsonValues -> {
          if (jsonValues.getFirst().isNull()) {
            return Optional.empty();
          }

          // We call getFirst() because there should only be one json value associated with this key
          // Then Redis should return in this order:
          // JsonValue(JSON Array) -> first and hopefully only JsonValue(JSON Object)
          JsonObject value = jsonValues.getFirst().asJsonArray().getFirst().asJsonObject();
          return Optional.ofNullable(deserializer.apply(value));
        });
  }

  @Override
  public CompletableFuture<Boolean> put(String key, DataModel value) {
    return commands.jsonSet(key, JsonPath.ROOT_PATH,
            value.serializeToJson(commands.getJsonParser()))
        .toCompletableFuture()
        .thenApply(result -> {
          if (result == null || !result.equals("OK")) {
            String message = "PUT operation returned non-OK response (key: %s)"
                .formatted(key);
            getLogger().warning("%s %s".formatted(getLogPrefix(), message));
            throw new CompletionException(new RuntimeException(message));
          }

          return true;
        });
  }

  @Override
  public CompletableFuture<Boolean> delete(String key) {
    return commands.del(key)
        .toCompletableFuture()
        .exceptionally(throwable -> {
          logDeleteError(key, throwable);
          throw new CompletionException(throwable);
        })
        .thenApply(result -> result > 0);
  }

  @Override
  public CompletableFuture<Boolean> flush(Set<String> keys) {
    String[] array = keys.toArray(String[]::new);

    // Lettuce throws a fit if we pass in an empty array
    if (array.length == 0) {
      return CompletableFuture.completedFuture(true);
    }

    return commands.del(array).toCompletableFuture()
        .thenCompose(result -> {
          logFlush(result.intValue());
          return CompletableFuture.completedFuture(true);
        });
  }

  @Override
  public double getHitRate() {
    return 0.0;
  }

  @Override
  public void shutdownCache() {
    connection.close();
    redisClient.shutdown();
    logShutdown();
  }

}

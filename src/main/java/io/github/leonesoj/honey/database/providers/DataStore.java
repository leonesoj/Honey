package io.github.leonesoj.honey.database.providers;

import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.record.DataRecord;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DataStore {

  private final Logger logger;

  private final DataProvider provider;

  public DataStore(Logger logger, DataProvider provider) {
    this.logger = logger;
    this.provider = provider;
  }

  protected Logger getLogger() {
    return logger;
  }

  public abstract CompletableFuture<Boolean> insert(DataContainer dataContainer,
      Map<String, Object> data);

  public abstract CompletableFuture<Boolean> update(DataContainer dataContainer, String index,
      Object value, Map<String, Object> data);

  public abstract <T> CompletableFuture<Optional<T>> query(DataContainer dataContainer,
      String index, String value, Function<DataRecord, T> mapper);

  public abstract <T> CompletableFuture<List<T>> queryMany(DataContainer dataContainer,
      String index, String value, int limit, int offset, Function<DataRecord, T> mapper);

  public abstract CompletableFuture<Boolean> delete(DataContainer dataContainer, String index,
      String value);

  public abstract void createDataStore(DataContainer dataContainer);

  public abstract void closeConnection();

  public DataProvider getProvider() {
    return provider;
  }

  protected void logInsertError(String containerName, Map<String, Object> data,
      Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s Insert operation failed (container: %s, data: %s)"
            .formatted(getLogPrefix(), containerName, data),
        throwable
    );
  }

  protected void logUpdateError(String containerName, String index, Map<String, Object> data,
      Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s Update operation failed (container: %s, index: %s, data: %s)"
            .formatted(getLogPrefix(), containerName, index, data),
        throwable
    );
  }

  protected void logQueryError(String containerName, String index, String value,
      Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s Query operation failed (container: %s, index: %s, value: %s)"
            .formatted(getLogPrefix(), containerName, index, value),
        throwable
    );
  }

  protected void logQueryManyError(String containerName, String index, String value, int limit,
      int offset, Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s QueryMany operation failed (container: %s, index: %s, value: %s, limit: %d, offset: %d)"
            .formatted(getLogPrefix(), containerName, index, value, limit, offset),
        throwable
    );
  }

  protected void logDeleteError(String containerName, String index, String value,
      Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s Delete operation failed (container: %s, index: %s, value: %s)"
            .formatted(getLogPrefix(), containerName, index, value),
        throwable
    );
  }

  protected void logCreateError(String containerName, Throwable throwable) {
    logger.log(Level.SEVERE,
        "%s Create data store operation failed (container: %s)"
            .formatted(getLogPrefix(), containerName),
        throwable
    );
  }

  protected void logInitialize() {
    logger.info("%s Successfully initialized data store".formatted(getLogPrefix()));
  }

  protected void logInitializeError(Throwable throwable) {

  }

  protected void logShutdown() {
    logger.info("%s Successfully shutdown data store".formatted(getLogPrefix()));
  }

  protected String getLogPrefix() {
    return "|%s - DATA|".formatted(provider.toString());
  }

}

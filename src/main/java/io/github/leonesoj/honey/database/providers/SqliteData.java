package io.github.leonesoj.honey.database.providers;

import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.impl.ResultSetRecord;
import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Logger;

public class SqliteData extends DataStore {

  private Connection writeConnection;
  private Connection readConnection;

  private final ExecutorService executorService;

  public SqliteData(Path pluginDataPath, String fileName, Logger logger) {
    super(logger, DataProvider.SQLITE);

    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException ignored) {
      getLogger().severe("%s Failed to load SQLite driver".formatted(getLogPrefix()));
    }

    try {
      File dbFile = new File("%s/db/%s.db".formatted(pluginDataPath, fileName));
      dbFile.getParentFile().mkdirs();

      String dbUrl = "jdbc:sqlite:%s?busy_timeout=3000".formatted(dbFile.getAbsolutePath());

      writeConnection = DriverManager.getConnection(dbUrl);
      try (Statement statement = writeConnection.createStatement()) {
        statement.execute("PRAGMA journal_mode=WAL");
      }

      readConnection = DriverManager.getConnection(dbUrl);
      try (Statement statement = readConnection.createStatement()) {
        statement.execute("PRAGMA journal_mode=WAL");
      }

      logInitialize();
    } catch (SQLException error) {
      getLogger().severe(
          "%s Failed to open database connection: %s"
              .formatted(getLogPrefix(), error.getMessage()));
    }

    executorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public CompletableFuture<Boolean> insert(DataContainer dataContainer, Map<String, Object> data) {
    String insertCommand = dataContainer.getInsertCommand(data);

    return CompletableFuture.supplyAsync(() -> {
      try (PreparedStatement statement = writeConnection.prepareStatement(insertCommand)) {
        int index = 1;
        for (Object value : data.values()) {
          bindValue(statement, index++, value);
        }

        statement.executeUpdate();
        return true;
      } catch (SQLException exception) {
        logInsertError(dataContainer.containerName(), data, exception);
        throw new CompletionException(exception);
      }
    }, executorService);
  }

  @Override
  public CompletableFuture<Boolean> update(DataContainer dataContainer, String index,
      Object indexValue, Map<String, Object> data) {
    if (data.size() > dataContainer.schema().size()) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException(
              "Data map contains more entries (%d) than allowed by the schema (%d)."
                  .formatted(data.size(), dataContainer.schema().size())
          )
      );
    }

    return CompletableFuture.supplyAsync(() -> {
      try (PreparedStatement statement = writeConnection.prepareStatement(
          dataContainer.getUpdateCommand(data, index))) {
        int idx = 1;
        for (Object value : data.values()) {
          bindValue(statement, idx++, value);
        }
        bindIndexParam(statement, idx, dataContainer, index, indexValue);

        statement.executeUpdate();
        return true;
      } catch (SQLException exception) {
        logUpdateError(dataContainer.containerName(), index, data, exception);
        throw new CompletionException(exception);
      }
    }, executorService);
  }

  @Override
  public <T> CompletableFuture<Optional<T>> query(DataContainer dataContainer, String index,
      Object value, Function<DataRecord, T> mapper) {
    String selectCommand = dataContainer.getSelectCommand(index, 1, 0);

    return CompletableFuture.supplyAsync(() -> {
      try (PreparedStatement statement = readConnection.prepareStatement(selectCommand)) {
        bindIndexParam(statement, 1, dataContainer, index, value);

        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return Optional.ofNullable(
                mapper.apply(new ResultSetRecord(rs, getProvider(), dataContainer.schema()))
            );
          }
        }

      } catch (SQLException exception) {
        logQueryError(dataContainer.containerName(), index, value, exception);
        throw new CompletionException(exception);
      }

      return Optional.empty();
    }, executorService);
  }

  @Override
  public <T> CompletableFuture<List<T>> queryMany(DataContainer dataContainer, String index,
      Object value, int limit, int offset, Function<DataRecord, T> mapper) {
    String selectCommand = dataContainer.getSelectCommand(index, limit, offset);

    return CompletableFuture.supplyAsync(() -> {
      List<T> result = new ArrayList<>();

      try (PreparedStatement statement = readConnection.prepareStatement(selectCommand)) {
        bindIndexParam(statement, 1, dataContainer, index, value);

        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            result.add(
                mapper.apply(new ResultSetRecord(rs, getProvider(), dataContainer.schema()))
            );
          }
        }
      } catch (SQLException exception) {
        logQueryManyError(dataContainer.containerName(), index, value, limit, offset, exception);
        throw new CompletionException(exception);
      }

      return result;
    }, executorService);
  }

  @Override
  public CompletableFuture<Boolean> delete(DataContainer dataContainer, String index,
      Object value) {
    String deleteCommand = dataContainer.getDeleteCommand(index);

    return CompletableFuture.supplyAsync(() -> {
      try (PreparedStatement statement = writeConnection.prepareStatement(deleteCommand)) {
        bindIndexParam(statement, 1, dataContainer, index, value);

        statement.executeUpdate();
        return true;
      } catch (SQLException exception) {
        logDeleteError(dataContainer.containerName(), index, value, exception);
        throw new CompletionException(exception);
      }
    }, executorService);
  }

  @Override
  public void createDataStore(DataContainer dataContainer) {
    String createCommand = dataContainer.getCreateCommand(getProvider());

    try (Statement statement = writeConnection.createStatement()) {
      // Create the table first
      statement.executeUpdate(createCommand);

      // Follow with our index creation commands
      for (String indexCommand : dataContainer.getIndexCommands(getProvider())) {
        statement.executeUpdate(indexCommand);
      }
    } catch (SQLException exception) {
      logCreateError(dataContainer.containerName(), exception);
    }

  }

  @Override
  public void closeConnection() {
    try {
      writeConnection.close();
      readConnection.close();
      logShutdown();
    } catch (SQLException e) {
      getLogger().severe("Failed to close connection: " + e.getMessage());
    }

    executorService.shutdown();
  }
}

package io.github.leonesoj.honey.database.providers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.impl.ResultSetRecord;
import java.sql.Connection;
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
import java.util.function.Function;
import java.util.logging.Logger;

public class MySqlData extends DataStore {

  private final HikariDataSource dataSource;

  public MySqlData(String host, int port, String database, String user,
      String password, Logger logger) {
    super(logger, DataProvider.MYSQL);

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(
        "jdbc:mysql://" + host + ":" + port + "/" + database + "?connectionTimeZone=UTC"
    );
    config.setUsername(user);
    config.setPassword(password);
    config.setPoolName("Honey");

    dataSource = new HikariDataSource(config);

    try (Connection connection = dataSource.getConnection()) {
      if (!connection.isValid(2)) {
        throw new SQLException("%s Failed to connect to MySQL database".formatted(getLogPrefix()));
      }
      logInitialize();
    } catch (SQLException exception) {
      logInitializeError(exception);
    }
  }

  @Override
  public CompletableFuture<Boolean> insert(DataContainer dataContainer, Map<String, Object> data) {
    String insertCommand = dataContainer.getInsertCommand(data);

    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(insertCommand)) {

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
    });
  }

  @Override
  public CompletableFuture<Boolean> update(DataContainer dataContainer, String index, Object value,
      Map<String, Object> data) {
    String updateCommand = dataContainer.getUpdateCommand(data, index);

    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(updateCommand)) {

        int idx = 1;
        for (Object objValue : data.values()) {
          bindValue(statement, idx++, objValue);
        }
        bindIndexParam(statement, idx, dataContainer, index, value);

        statement.executeUpdate();
        return true;
      } catch (SQLException exception) {
        logUpdateError(dataContainer.containerName(), index, data, exception);
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public <T> CompletableFuture<Optional<T>> query(DataContainer dataContainer, String index,
      Object value, Function<DataRecord, T> mapper) {
    String selectCommand = dataContainer.getSelectCommand(index);

    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(selectCommand)) {

        bindIndexParam(statement, 1, dataContainer, index, value);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
          return Optional.ofNullable(mapper.apply(new ResultSetRecord(resultSet, getProvider())));
        }

        return Optional.empty();
      } catch (SQLException exception) {
        logQueryError(dataContainer.containerName(), index, value, exception);
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public <T> CompletableFuture<List<T>> queryMany(DataContainer dataContainer, String index,
      Object value, int limit, int offset, Function<DataRecord, T> mapper) {
    String selectCommand = dataContainer.getSelectCommand(index, limit, offset);

    return CompletableFuture.supplyAsync(() -> {
      List<T> result = new ArrayList<>();

      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(selectCommand)) {

        bindIndexParam(statement, 1, dataContainer, index, value);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          result.add(mapper.apply(new ResultSetRecord(resultSet, getProvider())));
        }
      } catch (SQLException exception) {
        logQueryManyError(dataContainer.containerName(), index, value, limit, offset, exception);
        throw new CompletionException(exception);
      }

      return result;
    });
  }

  @Override
  public CompletableFuture<Boolean> delete(DataContainer dataContainer, String index,
      Object value) {
    String deleteCommand = dataContainer.getDeleteCommand(index);

    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(deleteCommand)) {

        bindIndexParam(statement, 1, dataContainer, index, value);

        statement.executeUpdate();
        return true;
      } catch (SQLException exception) {
        logDeleteError(dataContainer.containerName(), index, value, exception);
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public void createDataStore(DataContainer dataContainer) {
    String createCommand = dataContainer.getCreateCommand(getProvider());

    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      statement.executeUpdate(createCommand);

      for (String idxSql : dataContainer.getIndexCommands(getProvider())) {
        try {
          statement.executeUpdate(idxSql);
        } catch (SQLException e) {
          if (e.getErrorCode() != 1061) {
            throw e;
          }
        }
      }

    } catch (SQLException exception) {
      logCreateError(dataContainer.containerName(), exception);
    }
  }

  @Override
  public void closeConnection() {
    dataSource.close();
    logShutdown();
  }
}

package io.github.leonesoj.honey.database.providers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.ResultSetRecord;
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
    config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
    config.setUsername(user);
    config.setPassword(password);

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
        for (Map.Entry<String, Object> entry : data.entrySet()) {
          statement.setObject(index++, entry.getValue(),
              dataContainer.schema().get(entry.getKey()).getJdbcType());
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
        for (Map.Entry<String, Object> entry : data.entrySet()) {
          statement.setObject(idx++, entry.getValue(),
              dataContainer.schema().get(entry.getKey()).getJdbcType());
        }

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
      String value, Function<DataRecord, T> mapper) {
    String selectCommand = dataContainer.getSelectCommand(index);

    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(selectCommand)) {

        statement.setString(1, value);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
          return Optional.ofNullable(mapper.apply(new ResultSetRecord(resultSet)));
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
      String value, int limit, int offset, Function<DataRecord, T> mapper) {
    String selectCommand = dataContainer.getSelectCommand(index, limit, offset);

    return CompletableFuture.supplyAsync(() -> {
      List<T> result = new ArrayList<>();

      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(selectCommand)) {
        statement.setString(1, value);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          result.add(mapper.apply(new ResultSetRecord(resultSet)));
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
      String value) {
    String deleteCommand = dataContainer.getDeleteCommand(index);

    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(deleteCommand)) {
        statement.setString(1, value);
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
    String createCommand = dataContainer.getCreateCommand();

    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      statement.executeUpdate(createCommand);
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

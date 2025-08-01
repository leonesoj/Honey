package io.github.leonesoj.honey.database.providers;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.DocumentRecord;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bson.Document;

public class MongoData extends DataStore {

  private MongoClient mongoClient;
  private MongoDatabase mongoDatabase;

  public MongoData(String connectionString, Logger logger) {
    super(logger, DataProvider.MONGODB);

    try {
      mongoClient = MongoClients.create(MongoClientSettings.builder()
          .applyConnectionString(new ConnectionString(connectionString))
          .build()
      );
      mongoDatabase = mongoClient.getDatabase("honey");
      logInitialize();
    } catch (Exception e) {
      getLogger().severe("%s Failed to connect to Mongo database".formatted(getLogPrefix()));
    }
  }

  @Override
  public CompletableFuture<Boolean> insert(DataContainer dataContainer, Map<String, Object> data) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        MongoCollection<Document> collection = mongoDatabase.getCollection(
            dataContainer.containerName()
        );
        collection.insertOne(new Document(data));
        return true;
      } catch (MongoException exception) {
        logInsertError(dataContainer.containerName(), data, exception);
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public CompletableFuture<Boolean> update(DataContainer dataContainer, String index, Object value,
      Map<String, Object> data) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        MongoCollection<Document> collection = mongoDatabase.getCollection(
            dataContainer.containerName()
        );

        collection.updateOne(new Document(data), Filters.eq(index, data.get(index)));
        return true;
      } catch (MongoException exception) {
        logUpdateError(dataContainer.containerName(), index, data, exception);
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public <T> CompletableFuture<Optional<T>> query(DataContainer dataContainer, String index,
      String value, Function<DataRecord, T> mapper) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        MongoCollection<Document> collection = mongoDatabase.getCollection(
            dataContainer.containerName()
        );

        Document document = collection
            .find(Filters.eq(index, value))
            .first();

        if (document == null) {
          return Optional.empty();
        }

        return Optional.ofNullable(mapper.apply(new DocumentRecord(document)));
      } catch (MongoException exception) {
        logQueryError(dataContainer.containerName(), index, value, exception);
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public <T> CompletableFuture<List<T>> queryMany(DataContainer dataContainer, String index,
      String value, int limit, int offset, Function<DataRecord, T> mapper) {
    return null;
  }

  @Override
  public CompletableFuture<Boolean> delete(DataContainer dataContainer, String index,
      String value) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        mongoDatabase.getCollection(dataContainer.containerName())
            .deleteMany(Filters.eq(index, value));
        return true;
      } catch (MongoException exception) {
        logDeleteError(dataContainer.containerName(), index, value, exception);
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public void createDataStore(DataContainer dataContainer) {
    try {
      if (collectionExists(dataContainer.containerName())) {
        mongoDatabase.createCollection(dataContainer.containerName());
      }

      TransactionOptions transactionOptions = TransactionOptions.builder()
          .writeConcern(WriteConcern.MAJORITY)
          .build();

      try (ClientSession session = mongoClient.startSession()) {
        session.withTransaction(() -> {
          MongoCollection<Document> collection = mongoDatabase.getCollection(
              dataContainer.containerName()
          );

          collection.createIndex(
              Indexes.ascending(dataContainer.primaryIndex()),
              new IndexOptions().unique(true)
          );

          for (String index : dataContainer.indexes()) {
            collection.createIndex(Indexes.ascending(index));
          }
          return null;
        }, transactionOptions);
      }

    } catch (MongoException exception) {
      logCreateError(dataContainer.containerName(), exception);
    }
  }

  @Override
  public void closeConnection() {
    mongoClient.close();
  }

  private boolean collectionExists(String name) {
    for (String collectionName : mongoDatabase.listCollectionNames()) {
      if (collectionName.equals(name)) {
        return true;
      }
    }
    return false;
  }
}

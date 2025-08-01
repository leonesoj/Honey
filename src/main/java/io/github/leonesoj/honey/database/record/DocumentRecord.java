package io.github.leonesoj.honey.database.record;

import java.time.Duration;
import java.util.HashSet;
import java.util.UUID;
import org.bson.Document;

public class DocumentRecord implements DataRecord {

  private final Document document;

  public DocumentRecord(Document document) {
    this.document = document;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(String key, FieldType fieldType) {
    return switch (fieldType) {
      case STRING -> (T) document.getString(key);
      case INTEGER -> (T) document.getInteger(key);
      case BOOLEAN -> (T) document.getBoolean(key);
      case UUID -> (T) UUID.fromString(document.getString(key));
      case SET_OF_UUID -> (T) new HashSet<>(document.getList(key, String.class));
      case LIST -> (T) document.getList(key, String.class);
      case INSTANT -> (T) document.getDate(key).toInstant();
      case DURATION -> (T) Duration.ofMillis(document.getLong(key));
    };
  }
}

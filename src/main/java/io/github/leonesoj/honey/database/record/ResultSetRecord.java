package io.github.leonesoj.honey.database.record;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class ResultSetRecord implements DataRecord {

  private final ResultSet resultSet;

  public ResultSetRecord(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(String key, FieldType fieldType) {
    try {
      switch (fieldType) {
        case STRING:
          return (T) resultSet.getString(key);
        case INTEGER:
          int intValue = resultSet.getInt(key);
          return resultSet.wasNull() ? null : (T) Integer.valueOf(intValue);
        case BOOLEAN:
          boolean boolValue = resultSet.getBoolean(key);
          return resultSet.wasNull() ? null : (T) Boolean.valueOf(boolValue);
        case UUID:
          String uuidStr = resultSet.getString(key);
          return uuidStr != null ? (T) UUID.fromString(uuidStr) : null;
        case INSTANT:
          String iso = resultSet.getString(key);
          Instant instant = OffsetDateTime.parse(iso).toInstant();
          return (T) instant;
        case DURATION:
          return (T) Duration.ofMillis(resultSet.getLong(key));
        case SET_OF_UUID:
          String json = resultSet.getString(key);
          Gson gson = new Gson();

          return (T) new HashSet<>(gson.fromJson(json,
              new TypeToken<HashSet<UUID>>() {}.getType())
          );
        case LIST:
          return null;
        default:
          throw new UnsupportedOperationException("Unsupported field type: " + fieldType);
      }
    } catch (SQLException e) {
      Bukkit.getLogger().log(Level.SEVERE, "Error while reading record", e);
      throw new RuntimeException("Failed to read field '" + key + "' from ResultSet", e);
    }
  }
}


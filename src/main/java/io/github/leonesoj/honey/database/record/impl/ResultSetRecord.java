package io.github.leonesoj.honey.database.record.impl;

import io.github.leonesoj.honey.database.providers.DataProvider;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.FieldType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public record ResultSetRecord(ResultSet resultSet, DataProvider dialect,
                              Map<String, FieldType> schema) implements DataRecord {

  @Override
  public <T> T get(String key) {
    FieldType fieldType = schema.get(key);
    if (fieldType == null) {
      throw new IllegalArgumentException("Field '" + key + "' not found in schema");
    }

    try {
      return fieldType.read(resultSet, key, dialect);
    } catch (SQLException e) {
      Bukkit.getLogger().log(Level.SEVERE, "Error while reading record", e);
      throw new RuntimeException("Failed to read field '" + key + "' from ResultSet", e);
    }
  }
}

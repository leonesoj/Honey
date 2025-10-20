package io.github.leonesoj.honey.database.record.impl;

import io.github.leonesoj.honey.database.providers.DataProvider;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.FieldType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class ResultSetRecord implements DataRecord {

  private final ResultSet resultSet;
  private final DataProvider dialect;

  public ResultSetRecord(ResultSet resultSet, DataProvider dialect) {
    this.resultSet = resultSet;
    this.dialect = dialect;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key, FieldType fieldType) {
    try {
      return fieldType.read(resultSet, key, dialect);
    } catch (SQLException e) {
      Bukkit.getLogger().log(Level.SEVERE, "Error while reading record", e);
      throw new RuntimeException("Failed to read field '" + key + "' from ResultSet", e);
    }
  }
}


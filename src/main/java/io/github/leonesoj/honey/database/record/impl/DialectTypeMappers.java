package io.github.leonesoj.honey.database.record.impl;

import io.github.leonesoj.honey.database.providers.DataProvider;
import java.util.EnumMap;
import java.util.Map;

public final class DialectTypeMappers {
  private static final Map<DataProvider, DialectTypeMapper> MAP = new EnumMap<>(DataProvider.class);

  static {
    MAP.put(DataProvider.MYSQL, new MysqlTypeMapper());
    MAP.put(DataProvider.SQLITE, new SqliteTypeMapper());
  }

  public static DialectTypeMapper of(DataProvider dialect) {
    DialectTypeMapper m = MAP.get(dialect);
    if (m == null) {
      throw new IllegalArgumentException("No type mapper for dialect: " + dialect);
    }
    return m;
  }

  private DialectTypeMappers() {
  }
}

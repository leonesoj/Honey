package io.github.leonesoj.honey.database.record;

import java.sql.JDBCType;

public enum FieldType {
  STRING(JDBCType.VARCHAR),
  INTEGER(JDBCType.INTEGER),
  BOOLEAN(JDBCType.BOOLEAN),
  INSTANT(JDBCType.TIMESTAMP),
  DURATION(JDBCType.BIGINT),
  UUID(JDBCType.VARCHAR),
  LIST(JDBCType.VARCHAR), // JSON
  SET_OF_UUID(JDBCType.VARCHAR),; // JSON

  private final JDBCType jdbcType;

  FieldType(JDBCType jdbcType) {
    this.jdbcType = jdbcType;
  }

  public JDBCType getJdbcType() {
    return jdbcType;
  }
}

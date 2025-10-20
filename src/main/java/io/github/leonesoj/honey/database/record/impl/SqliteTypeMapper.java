package io.github.leonesoj.honey.database.record.impl;

import io.github.leonesoj.honey.database.record.FieldType;

public class SqliteTypeMapper implements DialectTypeMapper {

  @Override
  public String toSqlType(FieldType fieldType) {
    return switch (fieldType) {
      case STRING -> "TEXT";
      case INTEGER, DURATION -> "INTEGER";
      case BOOLEAN -> "INTEGER"; // 0/1
      case UUID -> "BLOB(16)";
      case INSTANT -> "INTEGER"; // epoch millis
      case LIST, SET_OF_UUID -> "TEXT"; // JSON string
      case HASH -> "BLOB";
    };
  }
}


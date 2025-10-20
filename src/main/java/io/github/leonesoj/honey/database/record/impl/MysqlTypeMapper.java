package io.github.leonesoj.honey.database.record.impl;

import io.github.leonesoj.honey.database.record.FieldType;

public class MysqlTypeMapper implements DialectTypeMapper {

  @Override
  public String toSqlType(FieldType fieldType) {
    return switch (fieldType) {
      case STRING -> "VARCHAR(255)";
      case INTEGER -> "INT";
      case BOOLEAN -> "BOOLEAN";
      case UUID -> "BINARY(16)";
      case INSTANT -> "DATETIME(6)";
      case DURATION -> "BIGINT";
      case LIST, SET_OF_UUID -> "JSON";
      case HASH -> "BINARY(32)";
    };
  }
}


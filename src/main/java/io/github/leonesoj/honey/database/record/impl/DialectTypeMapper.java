package io.github.leonesoj.honey.database.record.impl;

import io.github.leonesoj.honey.database.record.FieldType;

public interface DialectTypeMapper {

  String toSqlType(FieldType fieldType);
}


package io.github.leonesoj.honey.database.record;

import io.github.leonesoj.honey.database.providers.DataProvider;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ColumnReader<T> {

  T read(ResultSet rs, String column, DataProvider dialect) throws SQLException;
}

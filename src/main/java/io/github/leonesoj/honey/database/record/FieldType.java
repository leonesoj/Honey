package io.github.leonesoj.honey.database.record;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.leonesoj.honey.database.providers.DataProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public enum FieldType {
  STRING((rs, c, d) -> rs.getString(c)),
  INTEGER((rs, c, d) -> {
    int v = rs.getInt(c);
    return rs.wasNull() ? null : v;
  }),
  BOOLEAN((rs, c, d) -> {
    boolean b = rs.getBoolean(c);
    if (!rs.wasNull()) {
      return b;
    }
    int i = rs.getInt(c);
    return rs.wasNull() ? null : (i != 0);
  }),
  UUID((rs, c, d) -> {
    byte[] bytes = rs.getBytes(c);
    if (bytes != null && bytes.length == 16) {
      return fromBytes(bytes);
    }
    return null;
  }),
  INSTANT((rs, c, d) -> {
    switch (d) {
      case MYSQL: { // DATETIME(6) stored as UTC, no zone in DB
        LocalDateTime ldt = rs.getObject(c, LocalDateTime.class);
        return (ldt == null) ? null : ldt.atOffset(ZoneOffset.UTC).toInstant();
      }
      case SQLITE: { // INTEGER epoch millis
        long ms = rs.getLong(c);
        return rs.wasNull() ? null : Instant.ofEpochMilli(ms);
      }
      default: {
        return rs.getObject(c, Instant.class);
      }
    }
  }),

  DURATION((rs, c, d) -> {
    long millis = rs.getLong(c);
    return rs.wasNull() ? null : Duration.ofMillis(millis);
  }),

  LIST((rs, c, d) -> {
    String json = rs.getString(c);
    if (json == null || json.isEmpty()) {
      return null;
    }

    return new Gson().fromJson(json, new TypeToken<List<String>>() {
    }.getType());
  }),

  SET_OF_UUID((rs, c, d) -> {
    String json = rs.getString(c);
    if (json == null || json.isEmpty()) {
      return null;
    }

    Set<UUID> set = new Gson().fromJson(json, new TypeToken<Set<UUID>>() {
    }.getType());
    return (set == null) ? null : new HashSet<>(set);
  }),

  HASH((rs, c, d) -> {
    byte[] b = rs.getBytes(c);
    return (b == null || rs.wasNull()) ? null : b;
  });

  private final ColumnReader<?> reader;

  FieldType(ColumnReader<?> reader) {
    this.reader = reader;
  }

  @SuppressWarnings("unchecked")
  public <T> T read(ResultSet rs, String column, DataProvider dialect) throws SQLException {
    return (T) reader.read(rs, column, dialect);
  }

  private static UUID fromBytes(byte[] b) {
    long msb = 0;
    long lsb = 0;

    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (b[i] & 0xffL);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (b[i] & 0xffL);
    }

    return new UUID(msb, lsb);
  }
}

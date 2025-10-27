package io.github.leonesoj.honey.database.record;

public interface DataRecord {

  <T> T get(String key);
}

package io.github.leonesoj.honey.database.data.model;

import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ChatLog(UUID id, UUID player,
                      String message, Instant timestamp) implements DataModel {

  @Override
  public Map<String, Object> serialize() {
    return Map.of();
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    return null;
  }
}

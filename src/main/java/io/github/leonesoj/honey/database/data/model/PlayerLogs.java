package io.github.leonesoj.honey.database.data.model;

import io.github.leonesoj.honey.utils.other.JsonUtil;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlayerLogs(UUID uuid, List<String> chatHistory,
                         List<String> messagesHistory) implements DataModel {

  private static final String KEY_UUID = "uuid";
  private static final String KEY_CHAT_HISTORY = "chat_history";
  private static final String KEY_MESSAGES_HISTORY = "messages_history";

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();

    map.put(KEY_UUID, uuid.toString());
    map.put(KEY_CHAT_HISTORY, chatHistory);
    map.put(KEY_MESSAGES_HISTORY, messagesHistory);

    return map;
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    JsonObject json = parser.createJsonObject();

    json.put(KEY_UUID, parser.createJsonValue(uuid.toString()));
    json.put(KEY_CHAT_HISTORY, JsonUtil.toJsonArray(chatHistory, parser));
    json.put(KEY_MESSAGES_HISTORY, JsonUtil.toJsonArray(messagesHistory, parser));

    return json;
  }

  public DataModel deserializeFromJson(JsonObject json) {
    return new PlayerLogs(UUID.fromString(json.get(KEY_UUID).asString()),
        JsonUtil.fromJsonArray(json.get(KEY_CHAT_HISTORY).asJsonArray(), String.class),
        JsonUtil.fromJsonArray(json.get(KEY_MESSAGES_HISTORY).asJsonArray(), String.class)
    );
  }

}

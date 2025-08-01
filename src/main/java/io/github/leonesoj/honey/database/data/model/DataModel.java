package io.github.leonesoj.honey.database.data.model;

import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.util.Map;

public interface DataModel {

  Map<String, Object> serialize();

  JsonObject serializeToJson(JsonParser parser);

}

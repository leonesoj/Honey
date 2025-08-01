package io.github.leonesoj.honey.utils.other;

import io.lettuce.core.json.JsonArray;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

  private JsonUtil() {
  }

  public static JsonValue toJsonString(String str, JsonParser parser) {
    return parser.createJsonValue("\"%s\"".formatted(str));
  }

  public static JsonValue toJsonBoolean(boolean bool, JsonParser parser) {
    return parser.createJsonValue(Boolean.toString(bool));
  }

  public static <T> List<T> fromJsonArray(JsonArray json, Class<T> clazz) {
    List<T> list = new ArrayList<>();
    json.asList().forEach(element -> list.add(element.toObject(clazz)));
    return list;
  }

  public static JsonArray toJsonArray(List<?> list, JsonParser parser) {
    JsonArray array = parser.createJsonArray();
    list.forEach(element -> array.add(toJsonString(element.toString(), parser)));
    return array;
  }

}

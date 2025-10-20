package io.github.leonesoj.honey.database.data.model;

import com.google.gson.Gson;
import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.FieldType;
import io.github.leonesoj.honey.utils.other.JsonUtil;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerSettings implements DataModel {

  private static final String UUID_FIELD = "uuid";
  private static final String PRIVATE_MESSAGES_FIELD = "private_messages";
  private static final String CHAT_MESSAGES_FIELD = "chat_messages";
  private static final String PROFANITY_FILTER_FIELD = "profanity_filter";
  private static final String SOUND_ALERTS_FIELD = "sound_alerts";
  private static final String IGNORE_LIST_FIELD = "ignore_list";

  public static final String STORAGE_KEY = "player_settings";
  public static final String PRIMARY_KEY = UUID_FIELD;

  public static final Map<String, FieldType> SCHEMA = Map.of(
      UUID_FIELD, FieldType.UUID,
      PRIVATE_MESSAGES_FIELD, FieldType.BOOLEAN,
      CHAT_MESSAGES_FIELD, FieldType.BOOLEAN,
      PROFANITY_FILTER_FIELD, FieldType.BOOLEAN,
      SOUND_ALERTS_FIELD, FieldType.BOOLEAN,
      IGNORE_LIST_FIELD, FieldType.SET_OF_UUID
  );

  private final UUID uuid;

  private boolean privateMessages;
  private boolean chatMessages;
  private boolean profanityFilter;
  private boolean soundAlerts;

  private final Set<UUID> ignoreList;

  public PlayerSettings(UUID uuid, boolean privateMessages, boolean chatMessages,
      boolean profanityFilter, boolean soundAlerts, Set<UUID> ignoreList) {
    this.uuid = uuid;
    this.privateMessages = privateMessages;
    this.chatMessages = chatMessages;
    this.profanityFilter = profanityFilter;
    this.soundAlerts = soundAlerts;
    this.ignoreList = ignoreList;
  }

  public UUID getUniqueId() {
    return uuid;
  }

  public boolean hasPrivateMessages() {
    return privateMessages;
  }

  public void togglePrivateMessages() {
    privateMessages = !privateMessages;
  }

  public boolean hasChatMessages() {
    return chatMessages;
  }

  public void toggleChatMessages() {
    chatMessages = !chatMessages;
  }

  public boolean hasProfanityFilter() {
    return profanityFilter;
  }

  public void toggleProfanityFilter() {
    profanityFilter = !profanityFilter;
  }

  public boolean hasSoundAlerts() {
    return soundAlerts;
  }

  public void toggleSoundAlerts() {
    soundAlerts = !soundAlerts;
  }

  public Set<UUID> getIgnoreList() {
    return ignoreList;
  }

  public void addToIgnoreList(UUID uuid) {
    ignoreList.add(uuid);
  }

  public void removeFromIgnoreList(UUID uuid) {
    ignoreList.remove(uuid);
  }

  public Map<String, Object> serialize() {
    Map<String, Object> map = new LinkedHashMap<>();

    map.put(UUID_FIELD, uuid);
    map.put(PRIVATE_MESSAGES_FIELD, privateMessages);
    map.put(CHAT_MESSAGES_FIELD, chatMessages);
    map.put(PROFANITY_FILTER_FIELD, profanityFilter);
    map.put(SOUND_ALERTS_FIELD, soundAlerts);
    map.put(IGNORE_LIST_FIELD, new Gson().toJson(ignoreList));

    return map;
  }

  public static PlayerSettings deserialize(DataRecord record) {
    return new PlayerSettings(
        record.get(UUID_FIELD, SCHEMA.get(UUID_FIELD)),
        record.get(PRIVATE_MESSAGES_FIELD, SCHEMA.get(PRIVATE_MESSAGES_FIELD)),
        record.get(CHAT_MESSAGES_FIELD, SCHEMA.get(CHAT_MESSAGES_FIELD)),
        record.get(PROFANITY_FILTER_FIELD, SCHEMA.get(PROFANITY_FILTER_FIELD)),
        record.get(SOUND_ALERTS_FIELD, SCHEMA.get(SOUND_ALERTS_FIELD)),
        record.get(IGNORE_LIST_FIELD, SCHEMA.get(IGNORE_LIST_FIELD))
    );
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    JsonObject json = parser.createJsonObject();

    json.put(PRIMARY_KEY, JsonUtil.toJsonString(uuid.toString(), parser));
    json.put(PRIVATE_MESSAGES_FIELD, JsonUtil.toJsonBoolean(privateMessages, parser));
    json.put(CHAT_MESSAGES_FIELD, JsonUtil.toJsonBoolean(chatMessages, parser));
    json.put(PROFANITY_FILTER_FIELD, JsonUtil.toJsonBoolean(profanityFilter, parser));
    json.put(SOUND_ALERTS_FIELD, JsonUtil.toJsonBoolean(soundAlerts, parser));
    json.put(IGNORE_LIST_FIELD, JsonUtil.toJsonArray(ignoreList.stream().toList(), parser));

    return json;
  }

  public static PlayerSettings deserializeFromJson(JsonObject json) {
    return new PlayerSettings(
        UUID.fromString(json.get(PRIMARY_KEY).asString()),
        json.get(PRIVATE_MESSAGES_FIELD).asBoolean(),
        json.get(CHAT_MESSAGES_FIELD).asBoolean(),
        json.get(PROFANITY_FILTER_FIELD).asBoolean(),
        json.get(SOUND_ALERTS_FIELD).asBoolean(),
        new HashSet<>(
            JsonUtil.fromJsonArray(json.get(IGNORE_LIST_FIELD).asJsonArray(), UUID.class))
    );
  }

}

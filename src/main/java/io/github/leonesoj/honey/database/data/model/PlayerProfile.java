package io.github.leonesoj.honey.database.data.model;

import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.FieldType;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PlayerProfile implements DataModel {

  private static final String UUID_FIELD = "uuid";
  private static final String LAST_SEEN_FIELD = "last_seen";
  private static final String FIRST_SEEN_FIELD = "first_seen";
  private static final String PLAY_TIME_FIELD = "play_time";
  private static final String LAST_CONNECTED_FIELD = "last_connected";

  public static final String STORAGE_KEY = "player_profiles";
  public static final String PRIMARY_INDEX = UUID_FIELD;

  public static final Map<String, FieldType> SCHEMA = Map.of(
      UUID_FIELD, FieldType.UUID,
      LAST_SEEN_FIELD, FieldType.INSTANT,
      FIRST_SEEN_FIELD, FieldType.INSTANT,
      PLAY_TIME_FIELD, FieldType.INTEGER,
      LAST_CONNECTED_FIELD, FieldType.STRING
  );

  private final UUID uuid;

  private final Instant firstSeen;
  private Instant lastSeen;
  private Duration playTime;

  private String lastConnected;

  public PlayerProfile(UUID uuid, Instant lastSeen, Instant firstSeen, Duration playTime,
      String lastConnected) {
    this.uuid = uuid;
    this.lastSeen = lastSeen;
    this.firstSeen = firstSeen;
    this.playTime = playTime;
    this.lastConnected = lastConnected;
  }

  public UUID getUuid() {
    return uuid;
  }

  public Instant getFirstSeen() {
    return firstSeen;
  }

  public Instant getLastSeen() {
    return lastSeen;
  }

  public void setLastSeen(Instant lastSeen) {
    this.lastSeen = lastSeen;
  }


  public Duration getPlayTime() {
    return playTime;
  }

  public void addPlayTime(Duration duration) {
    playTime = playTime.plus(duration);
  }

  public String getLastConnected() {
    return lastConnected;
  }

  public void setLastConnected(String lastConnected) {
    this.lastConnected = lastConnected;
  }

  @Override
  public Map<String, Object> serialize() {
    return Map.of(
        UUID_FIELD, uuid.toString(),
        LAST_SEEN_FIELD, lastSeen,
        FIRST_SEEN_FIELD, firstSeen,
        PLAY_TIME_FIELD, playTime,
        LAST_CONNECTED_FIELD, lastConnected
    );
  }

  public static PlayerProfile deserialize(DataRecord record) {
    return new PlayerProfile(
        record.get(UUID_FIELD, SCHEMA.get(UUID_FIELD)),
        record.get(LAST_SEEN_FIELD, SCHEMA.get(LAST_SEEN_FIELD)),
        record.get(FIRST_SEEN_FIELD, SCHEMA.get(FIRST_SEEN_FIELD)),
        record.get(PLAY_TIME_FIELD, SCHEMA.get(PLAY_TIME_FIELD)),
        record.get(LAST_CONNECTED_FIELD, SCHEMA.get(LAST_CONNECTED_FIELD))
    );
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    throw new UnsupportedOperationException(
        "PlayerProfile data should not be serialized to JSON for the purpose of caching"
    );
  }
}

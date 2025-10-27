package io.github.leonesoj.honey.database.data.model;

import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.FieldType;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerProfile implements DataModel {

  private static final String UUID_FIELD = "uuid";
  private static final String HASHED_IP_FIELD = "hashed_ip";
  private static final String LAST_SEEN_FIELD = "last_seen";
  private static final String FIRST_SEEN_FIELD = "first_seen";
  private static final String PLAY_TIME_FIELD = "play_time";
  private static final String LAST_CONNECTED_FIELD = "last_connected";

  public static final String STORAGE_KEY = "player_profiles";
  public static final String PRIMARY_INDEX = UUID_FIELD;

  public static final Map<String, FieldType> SCHEMA = Map.of(
      UUID_FIELD, FieldType.UUID,
      HASHED_IP_FIELD, FieldType.HASH,
      LAST_SEEN_FIELD, FieldType.INSTANT,
      FIRST_SEEN_FIELD, FieldType.INSTANT,
      PLAY_TIME_FIELD, FieldType.DURATION,
      LAST_CONNECTED_FIELD, FieldType.STRING
  );

  public static final Set<String> INDEXED_FIELDS = Set.of(
      PRIMARY_INDEX,
      HASHED_IP_FIELD
  );

  private final UUID uuid;
  private final byte[] hashedIp;

  private final Instant firstSeen;
  private Instant lastSeen;
  private Duration playTime;

  private String lastConnected;

  public PlayerProfile(UUID uuid, byte[] hashedIp, Instant lastSeen, Instant firstSeen,
      Duration playTime, String lastConnected) {
    this.uuid = uuid;
    this.hashedIp = hashedIp;
    this.lastSeen = lastSeen;
    this.firstSeen = firstSeen;
    this.playTime = playTime;
    this.lastConnected = lastConnected;
  }

  public UUID getUuid() {
    return uuid;
  }

  public byte[] getHashedIp() {
    return hashedIp;
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
    HashMap<String, Object> map = new LinkedHashMap<>();

    map.put(UUID_FIELD, uuid);
    map.put(HASHED_IP_FIELD, hashedIp);
    map.put(LAST_SEEN_FIELD, lastSeen);
    map.put(FIRST_SEEN_FIELD, firstSeen);
    map.put(PLAY_TIME_FIELD, playTime);
    map.put(LAST_CONNECTED_FIELD, lastConnected);

    return map;
  }

  public static PlayerProfile deserialize(DataRecord record) {
    return new PlayerProfile(
        record.get(UUID_FIELD),
        record.get(HASHED_IP_FIELD),
        record.get(LAST_SEEN_FIELD),
        record.get(FIRST_SEEN_FIELD),
        record.get(PLAY_TIME_FIELD),
        record.get(LAST_CONNECTED_FIELD)
    );
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    throw new UnsupportedOperationException(
        "PlayerProfile data should not be serialized to JSON for the purpose of caching"
    );
  }
}

package io.github.leonesoj.honey.database.data.model;

import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.FieldType;
import io.github.leonesoj.honey.utils.other.JsonUtil;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffSettings implements DataModel {

  private static final String UUID_FIELD = "uuid";
  private static final String STAFF_MODE_FIELD = "staff_mode";
  private static final String VISIBLE_STAFF_FIELD = "visible_staff";
  private static final String SOCIALSPY_FIELD = "social_spy";
  private static final String REPORT_ALERTS_FIELD = "report_alerts";
  private static final String PERSIST_STAFF_MODE_FIELD = "persist_staff_mode";
  private static final String STAFF_ALERTS_FIELD = "staff_alerts";

  public static final String STORAGE_KEY = "staff_settings";
  public static final String PRIMARY_KEY = UUID_FIELD;

  public static final Map<String, FieldType> SCHEMA = Map.of(
      UUID_FIELD, FieldType.UUID,
      STAFF_MODE_FIELD, FieldType.BOOLEAN,
      VISIBLE_STAFF_FIELD, FieldType.BOOLEAN,
      SOCIALSPY_FIELD, FieldType.BOOLEAN,
      REPORT_ALERTS_FIELD, FieldType.BOOLEAN,
      PERSIST_STAFF_MODE_FIELD, FieldType.BOOLEAN,
      STAFF_ALERTS_FIELD, FieldType.BOOLEAN
  );

  private final UUID uuid;

  private boolean staffMode;
  private boolean visibleStaff;
  private boolean socialSpy;
  private boolean reportAlerts;
  private boolean persistStaffMode;
  private boolean staffAlerts;

  public StaffSettings(UUID uuid, boolean staffMode, boolean visibleStaff, boolean socialSpy,
      boolean reportAlerts, boolean persistStaffMode, boolean staffAlerts) {
    this.uuid = uuid;
    this.staffMode = staffMode;
    this.visibleStaff = visibleStaff;
    this.socialSpy = socialSpy;
    this.reportAlerts = reportAlerts;
    this.persistStaffMode = persistStaffMode;
    this.staffAlerts = staffAlerts;
  }

  public UUID getUniqueId() {
    return uuid;
  }

  public boolean inStaffMode() {
    return staffMode;
  }

  public void setStaffMode(boolean staffMode) {
    this.staffMode = staffMode;
  }

  public boolean hasVisibleStaff() {
    return visibleStaff;
  }

  public void toggleVisibleStaff() {
    visibleStaff = !visibleStaff;
  }

  public boolean hasSocialSpy() {
    return socialSpy;
  }

  public void setSocialSpy(boolean socialSpy) {
    this.socialSpy = socialSpy;
  }

  public boolean hasReportAlerts() {
    return reportAlerts;
  }

  public void toggleReportAlerts() {
    reportAlerts = !reportAlerts;
  }

  public boolean shouldPersistStaffMode() {
    return persistStaffMode;
  }

  public void togglePersistStaffMode() {
    persistStaffMode = !persistStaffMode;
  }

  public boolean hasStaffAlerts() {
    return staffAlerts;
  }

  public void toggleStaffAlerts() {
    staffAlerts = !staffAlerts;
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();

    map.put(UUID_FIELD, uuid);
    map.put(STAFF_MODE_FIELD, staffMode);
    map.put(VISIBLE_STAFF_FIELD, visibleStaff);
    map.put(SOCIALSPY_FIELD, socialSpy);
    map.put(REPORT_ALERTS_FIELD, reportAlerts);
    map.put(PERSIST_STAFF_MODE_FIELD, persistStaffMode);
    map.put(STAFF_ALERTS_FIELD, staffAlerts);

    return map;
  }

  public static StaffSettings deserialize(DataRecord record) {
    return new StaffSettings(
        record.get(UUID_FIELD, SCHEMA.get(UUID_FIELD)),
        record.get(STAFF_MODE_FIELD, SCHEMA.get(STAFF_MODE_FIELD)),
        record.get(VISIBLE_STAFF_FIELD, SCHEMA.get(VISIBLE_STAFF_FIELD)),
        record.get(SOCIALSPY_FIELD, SCHEMA.get(SOCIALSPY_FIELD)),
        record.get(REPORT_ALERTS_FIELD, SCHEMA.get(REPORT_ALERTS_FIELD)),
        record.get(PERSIST_STAFF_MODE_FIELD, SCHEMA.get(PERSIST_STAFF_MODE_FIELD)),
        record.get(STAFF_ALERTS_FIELD, SCHEMA.get(STAFF_ALERTS_FIELD))
    );
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    JsonObject json = parser.createJsonObject();

    json.put(PRIMARY_KEY, JsonUtil.toJsonString(uuid.toString(), parser));
    json.put(STAFF_MODE_FIELD, JsonUtil.toJsonBoolean(staffMode, parser));
    json.put(VISIBLE_STAFF_FIELD, JsonUtil.toJsonBoolean(visibleStaff, parser));
    json.put(SOCIALSPY_FIELD, JsonUtil.toJsonBoolean(socialSpy, parser));
    json.put(REPORT_ALERTS_FIELD, JsonUtil.toJsonBoolean(reportAlerts, parser));
    json.put(PERSIST_STAFF_MODE_FIELD, JsonUtil.toJsonBoolean(persistStaffMode, parser));
    json.put(STAFF_ALERTS_FIELD, JsonUtil.toJsonBoolean(staffAlerts, parser));

    return json;
  }

  public static StaffSettings deserializeFromJson(JsonObject json) {
    return new StaffSettings(
        UUID.fromString(json.get(PRIMARY_KEY).asString()),
        json.get(STAFF_MODE_FIELD).asBoolean(),
        json.get(VISIBLE_STAFF_FIELD).asBoolean(),
        json.get(SOCIALSPY_FIELD).asBoolean(),
        json.get(REPORT_ALERTS_FIELD).asBoolean(),
        json.get(PERSIST_STAFF_MODE_FIELD).asBoolean(),
        json.get(STAFF_ALERTS_FIELD).asBoolean()
    );
  }
}

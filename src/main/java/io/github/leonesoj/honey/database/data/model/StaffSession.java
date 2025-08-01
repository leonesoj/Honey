package io.github.leonesoj.honey.database.data.model;

import io.github.leonesoj.honey.utils.other.JsonUtil;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.util.Map;
import java.util.UUID;

public class StaffSession implements DataModel {

  private static final String PRIMARY_KEY = "uuid";
  private static final String STAFF_MODE_FIELD = "staff_mode";
  private static final String VISIBLE_STAFF_FIELD = "visible_staff";
  private static final String SOCIAL_SPY_FIELD = "social_spy";
  private static final String REPORT_ALERTS_FIELD = "report_alerts";

  private final UUID uuid;

  private boolean staffMode;
  private boolean visibleStaff;
  private boolean socialSpy;
  private boolean reportAlerts;

  public StaffSession(UUID uuid, boolean staffMode, boolean visibleStaff, boolean socialSpy,
      boolean reportAlerts) {
    this.uuid = uuid;
    this.staffMode = staffMode;
    this.visibleStaff = visibleStaff;
    this.socialSpy = socialSpy;
    this.reportAlerts = reportAlerts;
  }

  public boolean isInStaffMode() {
    return staffMode;
  }

  public void setStaffMode(boolean staffMode) {
    this.staffMode = staffMode;
  }

  public boolean isStaffVisible() {
    return visibleStaff;
  }

  public void setVisibleStaff(boolean visibleStaff) {
    this.visibleStaff = visibleStaff;
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

  public void setReportAlerts(boolean reportAlerts) {
    this.reportAlerts = reportAlerts;
  }

  @Override
  public Map<String, Object> serialize() {
    // Unused - this data doesn't need to persist outside of server lifecycles
    return null;
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    JsonObject json = parser.createJsonObject();

    json.put(PRIMARY_KEY, JsonUtil.toJsonString(uuid.toString(), parser));
    json.put(STAFF_MODE_FIELD, JsonUtil.toJsonBoolean(staffMode, parser));
    json.put(VISIBLE_STAFF_FIELD, JsonUtil.toJsonBoolean(visibleStaff, parser));
    json.put(SOCIAL_SPY_FIELD, JsonUtil.toJsonBoolean(socialSpy, parser));
    json.put(REPORT_ALERTS_FIELD, JsonUtil.toJsonBoolean(reportAlerts, parser));

    return json;
  }

  public static StaffSession deserializeFromJson(JsonObject json) {
    return new StaffSession(
        UUID.fromString(json.get(PRIMARY_KEY).asString()),
        json.get(STAFF_MODE_FIELD).asBoolean(),
        json.get(VISIBLE_STAFF_FIELD).asBoolean(),
        json.get(SOCIAL_SPY_FIELD).asBoolean(),
        json.get(REPORT_ALERTS_FIELD).asBoolean()
    );
  }
}

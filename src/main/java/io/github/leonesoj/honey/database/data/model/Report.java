package io.github.leonesoj.honey.database.data.model;

import io.github.leonesoj.honey.database.record.DataRecord;
import io.github.leonesoj.honey.database.record.FieldType;
import io.lettuce.core.json.JsonObject;
import io.lettuce.core.json.JsonParser;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Report implements DataModel {

  private static final String ID_FIELD = "id";
  public static final String ISSUER_FIELD = "issuer";
  public static final String SUBJECT_FIELD = "subject";
  private static final String SERVER_FIELD = "server";
  private static final String REASON_FIELD = "reason";
  public static final String STATUS_FIELD = "status";
  private static final String TIMESTAMP_FIELD = "timestamp";

  public static final String STORAGE_KEY = "reports";
  public static final String PRIMARY_KEY = ID_FIELD;

  public static final Map<String, FieldType> SCHEMA = Map.of(
      ID_FIELD, FieldType.UUID,
      ISSUER_FIELD, FieldType.UUID,
      SUBJECT_FIELD, FieldType.UUID,
      SERVER_FIELD, FieldType.STRING,
      REASON_FIELD, FieldType.STRING,
      STATUS_FIELD, FieldType.STRING,
      TIMESTAMP_FIELD, FieldType.INSTANT
  );

  public static final Set<String> INDEXED_FIELDS = Set.of(
      ISSUER_FIELD,
      SUBJECT_FIELD,
      STATUS_FIELD
  );

  private UUID id;
  private UUID issuer;
  private UUID subject;

  private String server;
  private String reason;

  private ReportStatus status;

  private Instant timestamp;

  public Report(UUID id, UUID issuer, UUID subject, String server, String reason,
      ReportStatus status, Instant timestamp) {
    this.id = id;
    this.issuer = issuer;
    this.subject = subject;
    this.server = server;
    this.reason = reason;
    this.status = status;
    this.timestamp = timestamp;
  }

  public UUID getId() {
    return id;
  }

  public UUID getIssuer() {
    return issuer;
  }

  public UUID getSubject() {
    return subject;
  }

  public String getServer() {
    return server;
  }

  public String getReason() {
    return reason;
  }

  public ReportStatus getStatus() {
    return status;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setIssuer(UUID issuer) {
    this.issuer = issuer;
  }

  public void setSubject(UUID subject) {
    this.subject = subject;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public void setStatus(ReportStatus status) {
    this.status = status;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(ID_FIELD, id);
    map.put(ISSUER_FIELD, issuer);
    map.put(SUBJECT_FIELD, subject);
    map.put(SERVER_FIELD, server);
    map.put(REASON_FIELD, reason);
    map.put(STATUS_FIELD, status.toString());
    map.put(TIMESTAMP_FIELD, timestamp);
    return map;
  }

  public static Report deserialize(DataRecord record) {
    return new Report(
        record.get(ID_FIELD, SCHEMA.get(ID_FIELD)),
        record.get(ISSUER_FIELD, SCHEMA.get(ISSUER_FIELD)),
        record.get(SUBJECT_FIELD, SCHEMA.get(SUBJECT_FIELD)),
        record.get(SERVER_FIELD, SCHEMA.get(SERVER_FIELD)),
        record.get(REASON_FIELD, SCHEMA.get(REASON_FIELD)),
        ReportStatus.valueOf(record.get(STATUS_FIELD, SCHEMA.get(STATUS_FIELD))),
        record.get(TIMESTAMP_FIELD, SCHEMA.get(TIMESTAMP_FIELD))
    );
  }

  @Override
  public JsonObject serializeToJson(JsonParser parser) {
    throw new UnsupportedOperationException(
        "Report data should not be serialized to JSON for the purpose of caching"
    );
  }

  public enum ReportStatus {
    NOTED,
    PENDING_REVIEW,
    RESOLVED
  }
}

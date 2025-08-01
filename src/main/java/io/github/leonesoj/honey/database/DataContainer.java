package io.github.leonesoj.honey.database;

import io.github.leonesoj.honey.database.record.FieldType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public record DataContainer(@NotNull String containerName, @NotNull String primaryIndex,
                            @NotNull Map<String, FieldType> schema, @NotNull Set<String> indexes) {

  public String getCreateCommand() {
    StringBuilder command = new StringBuilder();

    command.append("CREATE TABLE IF NOT EXISTS ").append(containerName).append(" (");
    schema.forEach((field, type) ->
        command.append(field)
            .append(" ")
            .append(type.getJdbcType())
            .append(", ")
    );
    command.append("PRIMARY KEY (").append(primaryIndex).append(")");
    command.append(")");

    return command.toString();
  }

  public String getInsertCommand(Map<String, Object> fields) {
    StringBuilder command = new StringBuilder();
    StringBuilder placeholders = new StringBuilder();

    command.append("INSERT INTO ").append(containerName).append(" (");

    fields.keySet().forEach(field -> {
      if (schema.containsKey(field)) {
        command.append(field).append(", ");
        placeholders.append("?, ");
      } else {
        throw new IllegalArgumentException("Field " + field + " not found in schema");
      }
    });

    command.setLength(command.length() - 2);
    placeholders.setLength(placeholders.length() - 2);

    command.append(") VALUES (").append(placeholders).append(")");

    return command.toString();
  }

  public List<String> getIndexCommands() {
    List<String> commands = new ArrayList<>();

    for (String index : indexes) {
      String command =
          "CREATE INDEX IF NOT EXISTS idx_%s ON %s (%s)".formatted(index, containerName, index);
      commands.add(command);
    }

    return commands;
  }

  public String getUpdateCommand(Map<String, Object> fields, String index) {
    StringBuilder command = new StringBuilder();

    command.append("UPDATE ").append(containerName).append(" SET ");

    fields.forEach((field, value) -> {
      if (schema.containsKey(field)) {
        command.append(field).append(" = ?, ");
      } else {
        throw new IllegalArgumentException("Field " + field + " not found in schema");
      }
    });
    command.setLength(command.length() - 2);

    command.append(" WHERE ").append(index).append(" = ?");

    return command.toString();
  }

  public String getSelectCommand(String index, int limit, int offset) {
    return "SELECT * FROM %s WHERE %s = ? LIMIT %d OFFSET %d"
        .formatted(containerName, index, limit, offset);
  }

  public String getSelectCommand(String index) {
    return getSelectCommand(index, 1, 0);
  }

  public String getDeleteCommand(String index) {
    return "DELETE FROM %s WHERE %s = ?".formatted(containerName, index);
  }

}

package io.github.leonesoj.honey.chat.variables;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class VariableRegistry {

  private final Map<String, ChatVariable> vars = new LinkedHashMap<>();

  public VariableRegistry register(ChatVariable var) {
    vars.put(Objects.requireNonNull(var).name(), var);
    return this;
  }

  public Component applyAll(Component input, Player player, boolean requirePermission) {
    for (ChatVariable var : vars.values()) {
      Component varReplacement = var.replacement(player);
      if (varReplacement.equals(Component.empty())) {
        continue;
      }
      if (requirePermission && !player.hasPermission("honey.chat.variables." + var.name())) {
        continue;
      }
      Component replacement = Component.textOfChildren(
          Component.text("[", NamedTextColor.GRAY),
          var.replacement(player),
          Component.text("]", NamedTextColor.GRAY)
      );

      Pattern pattern = Pattern.compile(Pattern.quote(wrap(var.name())), Pattern.CASE_INSENSITIVE);
      input = input.replaceText(builder ->
          builder.match(pattern).once().replacement(replacement)
      );
    }
    return input;
  }

  private String wrap(String variableName) {
    return "[" + variableName + "]";
  }
}

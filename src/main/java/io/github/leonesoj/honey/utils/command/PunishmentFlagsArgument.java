package io.github.leonesoj.honey.utils.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.leonesoj.honey.utils.command.PunishmentFlagsArgument.PunishmentFlag;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import java.util.HashMap;
import java.util.Map;

public class PunishmentFlagsArgument implements
    CustomArgumentType.Converted<Map<PunishmentFlag, Boolean>, String> {

  @Override
  public Map<PunishmentFlag, Boolean> convert(String nativeType) {
    Map<PunishmentFlag, Boolean> flags = new HashMap<>();

    String[] tokens = nativeType.trim().split("\\s+");
    for (String token : tokens) {
      if (token.startsWith("-")) {
        PunishmentFlag flag = PunishmentFlag.fromIdentifier(token.substring(1));
        if (flag != null) {
          flags.put(flag, true);
        } else {

        }
      }
    }

    return flags;
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.string();
  }

  public enum PunishmentFlag {
    SILENT("s"),
    PERMANENT("p"),
    FORCE("f"),
    IP_BAN("i");

    private final String identifier;

    PunishmentFlag(String identifier) {
      this.identifier = identifier;
    }

    public static PunishmentFlag fromIdentifier(String identifier) {
      for (PunishmentFlag flag : PunishmentFlag.values()) {
        if (flag.identifier.equalsIgnoreCase(identifier)) {
          return flag;
        }
      }
      return null;
    }

  }

}

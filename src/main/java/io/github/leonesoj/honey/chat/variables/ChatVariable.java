package io.github.leonesoj.honey.chat.variables;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface ChatVariable {
  String name();
  Component replacement(Player player);
}

package io.github.leonesoj.honey.chat.variables;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class PingVariable implements ChatVariable {

  @Override
  public String name() {
    return "ping";
  }

  @Override
  public Component replacement(Player player) {
    return Component.text("Ping: " + player.getPing() + "ms");
  }
}

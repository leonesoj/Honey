package io.github.leonesoj.honey.chat.variables;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationVariable implements ChatVariable {

  @Override
  public String name() {
    return "location";
  }

  @Override
  public Component replacement(Player player) {
    Location loc = player.getLocation();
    return Component.textOfChildren(
        Component.text("x: " + loc.getBlockX() + ", "),
        Component.text("y: " + loc.getBlockY() + ", "),
        Component.text("z: " + loc.getBlockZ())
    );
  }
}

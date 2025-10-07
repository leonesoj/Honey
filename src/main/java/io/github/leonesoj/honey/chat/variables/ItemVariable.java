package io.github.leonesoj.honey.chat.variables;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemVariable implements ChatVariable {

  @Override
  public String name() {
    return "item";
  }

  @Override
  public Component replacement(Player player) {
    ItemStack item = player.getInventory().getItemInMainHand();
    return !item.isEmpty() ? item.effectiveName().hoverEvent(item.asHoverEvent())
        : Component.empty();
  }
}

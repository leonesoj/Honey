package io.github.leonesoj.honey.commands.essentials.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

public class RepairCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("repair")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.essentials.repair"))
        .executes(RepairCommand::commandUsage)
        .then(Commands.literal("all").executes(RepairCommand::allUsage).build())
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ItemStack heldItem = sender.getInventory().getItemInMainHand();
    heldItem.editMeta(Damageable.class, Damageable::resetDamage);

    sender.sendMessage(Component.translatable("honey.repair"));
    return Command.SINGLE_SUCCESS;
  }

  private static int allUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    PlayerInventory inv = sender.getInventory();
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack item = inv.getItem(i);
      if (item != null) {
        item.editMeta(Damageable.class, Damageable::resetDamage);
        inv.setItem(i, item);
      }
    }

    sender.sendMessage(Component.translatable("honey.repair.all"));
    return Command.SINGLE_SUCCESS;
  }

}

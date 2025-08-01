package io.github.leonesoj.honey.commands.essentials.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.inventories.SettingsInventory;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class SettingsCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("settings")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.essentials.settings"))
        .executes(SettingsCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    new SettingsInventory(sender.getUniqueId(), sender.locale()).open(sender);
    return Command.SINGLE_SUCCESS;
  }

}

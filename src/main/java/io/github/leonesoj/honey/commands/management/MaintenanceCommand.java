package io.github.leonesoj.honey.commands.management;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public class MaintenanceCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("maintenance")
        .requires(stack -> stack.getSender()
            .hasPermission("honey.management.maintenance"))
        .executes(MaintenanceCommand::commandUsage)
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    return Command.SINGLE_SUCCESS;
  }

}

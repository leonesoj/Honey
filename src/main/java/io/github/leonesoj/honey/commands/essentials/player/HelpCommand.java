package io.github.leonesoj.honey.commands.essentials.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class HelpCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("help")
        .requires(stack -> stack.getSender() instanceof Player)
        .executes(HelpCommand::commandUsage)
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    ctx.getSource().getSender().sendPlainMessage("Our help command!");
    return Command.SINGLE_SUCCESS;
  }

}

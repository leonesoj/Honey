package io.github.leonesoj.honey.commands.moderation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class UnBanCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("unban")
        .requires(stack ->
            stack.getSender().hasPermission("honey.moderation.unban"))
        .then(Commands.argument("player", new OtherPlayerArgument()))
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    return Command.SINGLE_SUCCESS;
  }

}

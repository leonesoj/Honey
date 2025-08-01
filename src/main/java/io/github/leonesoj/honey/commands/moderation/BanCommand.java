package io.github.leonesoj.honey.commands.moderation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.github.leonesoj.honey.utils.command.PunishmentFlagsArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public class BanCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("ban")
        .requires(stack ->
            stack.getSender().hasPermission("honey.moderation.ban"))
        .then(Commands.argument("player", new OtherPlayerArgument())
            .then(Commands.argument("reason", StringArgumentType.string()))
            .then(Commands.argument("flags", new PunishmentFlagsArgument())))
        .executes(BanCommand::commandUsage)
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    return Command.SINGLE_SUCCESS;
  }

}

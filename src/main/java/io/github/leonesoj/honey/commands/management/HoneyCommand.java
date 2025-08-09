package io.github.leonesoj.honey.commands.management;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class HoneyCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("honey")
        .requires(stack -> stack.getSender().hasPermission("honey.admin"))
        .then(Commands.literal("reload").executes(HoneyCommand::configReloadUsage))
        .build();
  }

  private static int configReloadUsage(CommandContext<CommandSourceStack> ctx) {
    Honey.getInstance().getConfigHandler().reloadConfigs();
    Honey.getInstance().getChatService().getChannel("general").setFormat(
        Honey.getInstance().config().getString("chat.channels.general.format")
    );
    Honey.getInstance().getTranslationHandler().load();
    ctx.getSource().getSender().sendMessage(prefixed("honey.reload"));
    return Command.SINGLE_SUCCESS;
  }

}

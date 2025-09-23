package io.github.leonesoj.honey.commands.messaging;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MessageCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("message")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.messaging.message"))
        .then(Commands.argument("player", new OtherPlayerArgument())
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(MessageCommand::commandUsage))
        )
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    Player recipient = ctx.getArgument("player", Player.class);
    String message = ctx.getArgument("message", String.class);

    Honey.getInstance().getChatService().getPrivateChatService()
        .sendPrivateMessage(sender, recipient, Component.text(message));

    return Command.SINGLE_SUCCESS;
  }

}

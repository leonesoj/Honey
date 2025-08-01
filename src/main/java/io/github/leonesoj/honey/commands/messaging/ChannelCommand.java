package io.github.leonesoj.honey.commands.messaging;

import static io.github.leonesoj.honey.locale.Message.argComponent;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.ChatChannel;
import io.github.leonesoj.honey.utils.command.ChatChannelArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ChannelCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("channel")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.messaging.channel"))
        .then(Commands.argument("channel", new ChatChannelArgument())
            .executes(ChannelCommand::joinUsage))
        .build();
  }

  private static int joinUsage(CommandContext<CommandSourceStack> ctx) {
    ChatChannel channel = ctx.getArgument("channel", ChatChannel.class);
    Player sender = (Player) ctx.getSource().getSender();

    boolean joined = Honey.getInstance().getChatService()
        .changeChannel(channel, sender);
    if (joined) {
      sender.sendMessage(Component.translatable("honey.channel.switch",
          argComponent("channel", channel.getIdentifier())
      ));
    } else {
      sender.sendMessage(Component.translatable("honey.channel.disallowed.join",
          argComponent("channel", channel.getIdentifier())
      ));
    }
    return Command.SINGLE_SUCCESS;
  }

}

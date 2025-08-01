package io.github.leonesoj.honey.commands.moderation;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.ChatService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class SocialSpyCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("socialspy")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.management.socialspy"))
        .executes(SocialSpyCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ChatService chatService = Honey.getInstance().getChatService();

    if (!chatService.isSpy(sender)) {
      chatService.addSpy(sender);
      sender.sendMessage(prefixed("honey.socialspy.enabled"));
    } else {
      chatService.removeSpy(sender);
      sender.sendMessage(prefixed("honey.socialspy.disabled"));
    }

    return Command.SINGLE_SUCCESS;
  }

}

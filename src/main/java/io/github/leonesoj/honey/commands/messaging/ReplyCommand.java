package io.github.leonesoj.honey.commands.messaging;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.messaging.PrivateChatService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ReplyCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("reply")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.messaging.message"))
        .then(Commands.argument("message", StringArgumentType.greedyString())
            .executes(ReplyCommand::commandUsage)
        )
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    String message = ctx.getArgument("message", String.class);

    PrivateChatService chatService = Honey.getInstance().getPrivateChatService();
    UUID lastContactUuid = chatService.getLastContact(sender.getUniqueId());

    if (lastContactUuid == null) {
      sender.sendMessage(Component.translatable("honey.messaging.reply.none"));
      return Command.SINGLE_SUCCESS;
    }

    Player target = Bukkit.getPlayer(lastContactUuid);

    if (target == null || !target.isOnline()) {
      sender.sendMessage(Component.translatable("honey.messaging.reply.offline"));
      return Command.SINGLE_SUCCESS;
    }

    chatService.sendPrivateMessage(sender, target, Component.text(message));

    return Command.SINGLE_SUCCESS;
  }

}

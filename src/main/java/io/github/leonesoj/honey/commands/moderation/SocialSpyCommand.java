package io.github.leonesoj.honey.commands.moderation;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class SocialSpyCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("socialspy")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.moderation.socialspy"))
        .executes(SocialSpyCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    boolean newStatus = Honey.getInstance().getStaffHandler()
        .getSpyService()
        .toggleGlobalSpy(sender.getUniqueId());
    if (newStatus) {
      sender.sendMessage(prefixed("honey.socialspy.enabled"));
    } else {
      sender.sendMessage(prefixed("honey.socialspy.disabled"));
    }

    return Command.SINGLE_SUCCESS;
  }

}

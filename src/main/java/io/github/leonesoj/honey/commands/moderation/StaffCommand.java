package io.github.leonesoj.honey.commands.moderation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class StaffCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("staff")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.management.staff"))
        .executes(StaffCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    Honey.getInstance().getStaffHandler().toggleStaffMode(sender);
    return Command.SINGLE_SUCCESS;
  }

}

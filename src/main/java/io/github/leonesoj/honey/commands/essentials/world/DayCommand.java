package io.github.leonesoj.honey.commands.essentials.world;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.locale.Message;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

public class DayCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("day")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.world.day")
            && sender.getWorld().getEnvironment().equals(Environment.NORMAL))
        .executes(DayCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> context) {
    Player sender = (Player) context.getSource().getSender();
    sender.getWorld().setTime(1000L);

    sender.sendMessage(Message.prefixed("honey.world.day"));
    return Command.SINGLE_SUCCESS;
  }

}

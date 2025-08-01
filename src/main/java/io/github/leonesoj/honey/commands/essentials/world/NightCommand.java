package io.github.leonesoj.honey.commands.essentials.world;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.locale.Message;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

public class NightCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("night")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.world.night")
            && sender.getWorld().getEnvironment().equals(Environment.NORMAL))
        .executes(NightCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    sender.getWorld().setTime(13000L);

    sender.sendMessage(Message.prefixed("honey.world.night"));
    return Command.SINGLE_SUCCESS;
  }

}

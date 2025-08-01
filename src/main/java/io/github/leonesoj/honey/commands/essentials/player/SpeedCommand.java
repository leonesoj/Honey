package io.github.leonesoj.honey.commands.essentials.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class SpeedCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("speed")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.player.speed"))
        .then(Commands.literal("walk")
            .then(Commands.argument("speed", FloatArgumentType.floatArg(0, 1.0F))
                .executes(ctx -> commandUsage(ctx, "walk"))))
        .then(Commands.literal("flight").then(
            Commands.argument("speed", FloatArgumentType.floatArg(0, 1.0F))
                .executes(ctx -> commandUsage(ctx, "flight"))))
        .then(Commands.literal("reset").executes(SpeedCommand::resetUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx, String type) {
    float speed = FloatArgumentType.getFloat(ctx, "speed");
    Player sender = (Player) ctx.getSource().getSender();

    if (type.equals("walk")) {
      sender.setWalkSpeed(speed);
    } else if (type.equals("flight")) {
      sender.setFlySpeed(speed);
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int resetUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    sender.setWalkSpeed(0.2F);
    sender.setFlySpeed(0.1F);
    return Command.SINGLE_SUCCESS;
  }
}

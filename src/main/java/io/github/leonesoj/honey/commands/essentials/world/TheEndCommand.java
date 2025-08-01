package io.github.leonesoj.honey.commands.essentials.world;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.locale.Message;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

public class TheEndCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("theend")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.world.theend")
            && !sender.getWorld().getEnvironment().equals(Environment.THE_END)
            && Bukkit.getAllowEnd())
        .executes(TheEndCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    // Coordinates where Minecraft would generate the end platform upon portal entry.
    Location platform = new Location(
        Bukkit.getWorld("world_the_end"),
        100,
        49,
        0
    );
    sender.teleportAsync(platform);

    sender.sendMessage(Message.prefixed("honey.theend.teleport"));
    return Command.SINGLE_SUCCESS;
  }

}

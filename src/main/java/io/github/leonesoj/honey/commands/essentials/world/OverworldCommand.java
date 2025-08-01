package io.github.leonesoj.honey.commands.essentials.world;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.locale.Message;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class OverworldCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("overworld")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.world.overworld")
            && !sender.getWorld().getEnvironment().equals(Environment.NORMAL))
        .executes(OverworldCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    Environment env = sender.getWorld().getEnvironment();
    if (env.equals(Environment.NETHER)) {
      Location fromNether = new Location(
          Bukkit.getWorld("world"),
          sender.getLocation().getX() * 8,
          sender.getLocation().getY(),
          sender.getLocation().getZ() * 8
      );
      sender.teleportAsync(fromNether, TeleportCause.PLUGIN);
    } else {
      sender.teleportAsync(
          Objects.requireNonNullElse(sender.getRespawnLocation(),
              Bukkit.getWorlds().getFirst().getSpawnLocation()),
          TeleportCause.PLUGIN
      );
    }
    sender.sendMessage(Message.prefixed("honey.overworld.teleport"));

    return Command.SINGLE_SUCCESS;
  }

}

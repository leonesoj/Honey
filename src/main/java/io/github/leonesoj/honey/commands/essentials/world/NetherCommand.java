package io.github.leonesoj.honey.commands.essentials.world;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class NetherCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("nether")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.world.nether")
            && !sender.getWorld().getName().equals("world_nether")
            && Bukkit.getAllowNether())
        .executes(NetherCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    Location location = sender.getLocation();
    location.setWorld(Bukkit.getWorld("world_nether"));
    location.setX(location.getX() / 8);
    location.setZ(location.getZ() / 8);

    sender.teleportAsync(location, TeleportCause.PLUGIN);
    sender.sendMessage(prefixed("honey.nether.teleport"));
    return Command.SINGLE_SUCCESS;
  }

}

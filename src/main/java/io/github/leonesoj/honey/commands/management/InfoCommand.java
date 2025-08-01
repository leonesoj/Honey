package io.github.leonesoj.honey.commands.management;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class InfoCommand {

  public static LiteralCommandNode<CommandSourceStack> createCommand() {
    return Commands.literal("info")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.management.whois"))
        .then(Commands.argument("target", StringArgumentType.word())
            .executes(InfoCommand::commandUsage))
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    String target = ctx.getArgument("target", String.class);
    Player sender = (Player) ctx.getSource().getSender();

    Player onlinePlayer = Bukkit.getPlayer(target);
    if (onlinePlayer != null) {

    }

    return Command.SINGLE_SUCCESS;
  }

  private static void sendOverview(Player sender, OfflinePlayer player) {
  }

}

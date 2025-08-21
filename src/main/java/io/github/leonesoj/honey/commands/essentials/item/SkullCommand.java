package io.github.leonesoj.honey.commands.essentials.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.utils.command.ValidUsernameArgument;
import io.github.leonesoj.honey.utils.item.HeadUtils;
import io.github.leonesoj.honey.utils.item.ItemBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("skull")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.item.skull"))
        .executes(SkullCommand::commandUsage)
        .then(Commands.argument("player", new ValidUsernameArgument())
            .executes(SkullCommand::targetUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    sender.getInventory().addItem(getSkull(sender, sender.getPlayerProfile()));
    return Command.SINGLE_SUCCESS;
  }

  private static int targetUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    String target = ctx.getArgument("player", String.class);

    Player onlinePlayer = Bukkit.getPlayer(target);
    if (onlinePlayer != null) {
      sender.getInventory().addItem(getSkull(onlinePlayer, onlinePlayer.getPlayerProfile()));
      return Command.SINGLE_SUCCESS;
    }

    HeadUtils.getPlayerHead(new ItemBuilder(Material.PLAYER_HEAD, 1), target,
        item -> sender.getInventory().addItem(item.build()));
    return Command.SINGLE_SUCCESS;
  }

  private static ItemStack getSkull(OfflinePlayer player, PlayerProfile profile) {
    ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
    itemStack.editMeta(SkullMeta.class, meta -> {
      meta.setOwningPlayer(player);
      meta.setPlayerProfile(profile);
    });

    return itemStack;
  }

}

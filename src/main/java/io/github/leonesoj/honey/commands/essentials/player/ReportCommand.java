package io.github.leonesoj.honey.commands.essentials.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.inventories.ReportInventory;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.github.leonesoj.honey.utils.other.CooldownService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class ReportCommand {

  private static final CooldownService cooldownService = new CooldownService(5L);

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("report")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.essentials.report"))
        .then(Commands.argument("player", new OtherPlayerArgument(true))
            .executes(ReportCommand::commandUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player target = ctx.getArgument("player", Player.class);
    Player sender = (Player) ctx.getSource().getSender();

    new ReportInventory(target.getUniqueId(), sender.getUniqueId(), sender.locale())
        .open(sender.getPlayer());
    return Command.SINGLE_SUCCESS;
  }

}

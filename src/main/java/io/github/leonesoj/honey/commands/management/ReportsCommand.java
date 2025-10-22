package io.github.leonesoj.honey.commands.management;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.inventories.ReportViewInventory;
import io.github.leonesoj.honey.inventories.ReportsInventory;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ReportsCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("reports")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.management.reports"))
        .then(Commands.argument("id", ArgumentTypes.uuid())
            .executes(ReportsCommand::withIdUsage))
        .executes(ReportsCommand::commandUsage)
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    new ReportsInventory(sender.locale(), 0).open(sender);
    return Command.SINGLE_SUCCESS;
  }

  private static int withIdUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    UUID uuid = ctx.getArgument("id", UUID.class);
    Honey.getInstance().getDataHandler().getReportController().getReport(uuid)
        .thenAccept(optional -> {
          if (optional.isEmpty()) {
            sender.sendMessage(prefixed("honey.report.invalid"));
          } else {
            Bukkit.getGlobalRegionScheduler().run(Honey.getInstance(), task ->
                new ReportViewInventory(optional.get(), sender.locale(), null).open(sender)
            );
          }
        });
    return Command.SINGLE_SUCCESS;
  }

}

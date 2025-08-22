package io.github.leonesoj.honey.commands.essentials.player;

import static io.github.leonesoj.honey.locale.Message.argComponent;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.inventories.ReportInventory;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.github.leonesoj.honey.utils.other.CooldownService;
import io.github.leonesoj.honey.utils.other.DurationUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ReportCommand {

  private static final CooldownService cooldownService = new CooldownService();

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
    UUID senderUuid = sender.getUniqueId();

    if (cooldownService.hasCooldown(senderUuid)) {
      sender.sendMessage(
          Component.translatable("honey.command.cooldown",
              argComponent("duration",
                  DurationUtil.format(cooldownService.getRemaining(senderUuid)))
          ));
      return Command.SINGLE_SUCCESS;
    }

    new ReportInventory(target.getUniqueId(), senderUuid, sender.locale()).open(sender);
    return Command.SINGLE_SUCCESS;
  }

  public static CooldownService getCooldownService() {
    return cooldownService;
  }

}

package io.github.leonesoj.honey.commands.moderation;

import static io.github.leonesoj.honey.locale.Message.argComponent;
import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.SpyService;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class SpyCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("spy")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.moderation.socialspy"))
        .then(Commands.argument("player", new OtherPlayerArgument(true))
            .executes(SpyCommand::commandUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    Player target = ctx.getArgument("player", Player.class);

    UUID senderId = sender.getUniqueId();
    UUID targetId = target.getUniqueId();

    SpyService spyService = Honey.getInstance().getSpyService();

    Set<UUID> currentTargets = spyService.getTargetsOfSpy(senderId);
    if (!currentTargets.contains(targetId)) {
      spyService.trackAsSpy(senderId, targetId);
      sender.sendMessage(prefixed("honey.spy.enabled",
          argComponent("player", target.getName()))
      );
    } else {
      spyService.untrackSpyTarget(senderId, targetId);
      sender.sendMessage(prefixed("honey.spy.disabled",
          argComponent("player", target.getName()))
      );
    }

    return Command.SINGLE_SUCCESS;
  }

}

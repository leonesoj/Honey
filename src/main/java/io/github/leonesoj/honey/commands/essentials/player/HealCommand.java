package io.github.leonesoj.honey.commands.essentials.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.locale.Message;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HealCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("heal")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.player.heal"))
        .executes(HealCommand::commandUsage)
        .then(Commands.argument("player", ArgumentTypes.player())
            .executes(HealCommand::targetUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    healPlayer(sender);
    sender.sendMessage(Message.prefixed("honey.heal.self"));

    return Command.SINGLE_SUCCESS;
  }

  private static int targetUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = ctx.getArgument("player",
        PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(ctx.getSource()).getFirst();
    Player sender = (Player) ctx.getSource().getSender();

    healPlayer(target);
    sender.sendMessage(Message.prefixed("honey.heal.target",
        Message.argComponent("player", target.name()))
    );

    return Command.SINGLE_SUCCESS;
  }

  private static void healPlayer(Player player) {
    player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
    player.setFireTicks(0);
    player.setFoodLevel(20);
  }

}

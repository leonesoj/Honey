package io.github.leonesoj.honey.commands.essentials.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import static io.github.leonesoj.honey.locale.Message.prefixed;
import static io.github.leonesoj.honey.locale.Message.argComponent;

public class FlyCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("fly")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.player.fly"))
        .executes(FlyCommand::commandUsage)
        .then(Commands.argument("target", ArgumentTypes.player())
            .executes(FlyCommand::targetUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    boolean newStatus = invertFlightMode(sender);
    sender.sendMessage(prefixed("honey.fly.self." + fancyStatus(newStatus)));

    return Command.SINGLE_SUCCESS;
  }

  private static int targetUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = ctx.getArgument("target",
        PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(ctx.getSource()).getFirst();
    Player sender = (Player) ctx.getSource().getSender();
    boolean newStatus = invertFlightMode(target);

    sender.sendMessage(prefixed("honey.fly.target." + fancyStatus(newStatus),
        argComponent("player", target.getName()))
    );

    return Command.SINGLE_SUCCESS;
  }

  private static boolean invertFlightMode(Player player) {
    boolean newStatus = !player.getAllowFlight();
    player.setAllowFlight(newStatus);
    return newStatus;
  }

  private static String fancyStatus(boolean status) {
    return status ? "enabled" : "disabled";
  }

}

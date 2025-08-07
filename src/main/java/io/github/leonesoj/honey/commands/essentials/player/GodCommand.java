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
import org.bukkit.entity.Player;

public class GodCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("god")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.player.god"))
        .executes(GodCommand::commandUsage)
        .then(Commands.argument("target", ArgumentTypes.player())
            .executes(GodCommand::targetUsage))
        .build();
  }


  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    boolean status = invertGodMode(sender);
    sender.sendMessage(Message.prefixed("honey.god.self." + Message.fancyStatus(status)));

    return Command.SINGLE_SUCCESS;
  }

  private static int targetUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = ctx.getArgument("target",
        PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(ctx.getSource()).getFirst();
    Player sender = (Player) ctx.getSource().getSender();

    boolean status = invertGodMode(target);
    sender.sendMessage(Message.prefixed("honey.god.target." + Message.fancyStatus(status),
        Message.argComponent("player", target.getName()))
    );

    return Command.SINGLE_SUCCESS;
  }

  private static boolean invertGodMode(Player player) {
    boolean newStatus = !player.isInvulnerable();
    player.setInvulnerable(newStatus);
    return newStatus;
  }


}

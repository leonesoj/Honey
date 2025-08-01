package io.github.leonesoj.honey.commands.moderation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

public class InvseeCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("invsee")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.moderation.invsee"))
        .then(Commands.argument("player", ArgumentTypes.player())
            .executes(InvseeCommand::commandUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = ctx.getArgument("player",
        PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(ctx.getSource()).getFirst();
    Player sender = (Player) ctx.getSource().getSender();

    target.openInventory(sender.getInventory());

    return Command.SINGLE_SUCCESS;
  }

}

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

public class FeedCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("feed")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.player.feed"))
        .executes(FeedCommand::commandUsage)
        .then(Commands.argument("player", ArgumentTypes.player())
            .executes(FeedCommand::targetUsage))
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    feedPlayer(sender);
    return Command.SINGLE_SUCCESS;
  }

  public static int targetUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = ctx.getArgument(
        "player", PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(ctx.getSource()).getFirst();
    Player sender = (Player) ctx.getSource().getSender();

    feedPlayer(target);
    sender.sendMessage(Message.prefixed("honey.feed.other",
        Message.argComponent("player", target.getName())
    ));
    return Command.SINGLE_SUCCESS;
  }

  private static void feedPlayer(Player player) {
    player.setFoodLevel(20);
    player.setExhaustion(0F);
    player.setSaturation(10);
  }


}

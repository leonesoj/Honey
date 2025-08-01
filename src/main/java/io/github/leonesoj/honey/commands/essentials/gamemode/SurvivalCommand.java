package io.github.leonesoj.honey.commands.essentials.gamemode;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SurvivalCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("survival")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.gamemode.survival"))
        .executes(SurvivalCommand::selfUsage)
        .then(Commands.argument("target", ArgumentTypes.player())
            .executes(SurvivalCommand::targetUsage))
        .build();
  }

  private static int selfUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    sender.setGameMode(GameMode.SURVIVAL);
    sender.sendMessage(prefixed("honey.survival.yourself"));

    return Command.SINGLE_SUCCESS;
  }

  private static int targetUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = ctx.getArgument(
        "target", PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(ctx.getSource()).getFirst();
    Player sender = (Player) ctx.getSource().getSender();

    target.setGameMode(GameMode.SURVIVAL);
    sender.sendMessage(prefixed("honey.survival.other",
        Argument.component("player", Component.text(target.getName()))
    ));

    return Command.SINGLE_SUCCESS;
  }
}

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

public class CreativeCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("creative")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.gamemode.creative"))
        .executes(CreativeCommand::selfUsage)
        .then(Commands.argument("target", ArgumentTypes.player())
            .executes(CreativeCommand::targetUsage))
        .build();
  }

  private static int selfUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    sender.setGameMode(GameMode.CREATIVE);
    sender.sendMessage(prefixed("honey.creative.yourself"));
    return Command.SINGLE_SUCCESS;
  }

  private static int targetUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = ctx.getArgument(
        "target", PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(ctx.getSource()).getFirst();
    Player sender = (Player) ctx.getSource().getSender();

    target.setGameMode(GameMode.CREATIVE);
    sender.sendMessage(prefixed("honey.creative.other",
        Argument.component("player", Component.text(target.getName()))
    ));

    return Command.SINGLE_SUCCESS;
  }

}

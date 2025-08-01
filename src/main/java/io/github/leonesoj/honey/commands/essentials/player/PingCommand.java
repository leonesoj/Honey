package io.github.leonesoj.honey.commands.essentials.player;

import static io.github.leonesoj.honey.locale.Message.argComponent;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class PingCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("ping")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.essentials.ping"))
        .executes(PingCommand::commandUsage)
        .then(Commands.argument("target", ArgumentTypes.player())
            .executes(PingCommand::targetUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> context) {
    Player sender = (Player) context.getSource().getSender();

    sender.sendMessage(Component.translatable("honey.ping.self",
        argComponent("ping", sender.getPing())
    ));
    return Command.SINGLE_SUCCESS;
  }

  private static int targetUsage(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    PlayerSelectorArgumentResolver resolver = context.getArgument("target",
        PlayerSelectorArgumentResolver.class);
    Player target = resolver.resolve(context.getSource()).getFirst();
    Player sender = (Player) context.getSource().getSender();

    sender.sendMessage(Component.translatable("honey.ping.other",
        argComponent("player", target.getName()),
        argComponent("ping", target.getPing())
    ));
    return Command.SINGLE_SUCCESS;
  }

}

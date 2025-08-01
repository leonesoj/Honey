package io.github.leonesoj.honey.commands.management;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class BackendCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("backend")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.management.backend"))
        .then(Commands.literal("config-reload").executes(BackendCommand::configReloadUsage))
        .build();
  }

  private static int configReloadUsage(CommandContext<CommandSourceStack> ctx) {
    Honey.getInstance().getConfigHandler().reloadConfigs();
    Honey.getInstance().getChatService().getChannel("general").setFormat(
        Honey.getInstance().config().getString("chat.channels.general.format")
    );
    Honey.getInstance().getTranslationHandler().load();
    ctx.getSource().getSender().sendPlainMessage("Config's reloaded");
    return Command.SINGLE_SUCCESS;
  }
}

package io.github.leonesoj.honey.commands.management;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HoneyCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("honey")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.admin"))
        .then(Commands.literal("reload").executes(HoneyCommand::configReloadUsage))
        .build();
  }

  private static int configReloadUsage(CommandContext<CommandSourceStack> ctx) {
    UUID playerUuid = ((Player) ctx.getSource().getSender()).getUniqueId();
    Honey honey = Honey.getInstance();

    honey.getConfigHandler().reloadConfigs()
        .thenRun(() -> {
          honey.getChatService().getChannel("general").setFormat(
              honey.config().getString("chat.channels.general.format")
          );
          honey.getChatService().getChannel("staff").setFormat(
              honey.config().getString("chat.channels.staff.format")
          );
          sendMessageAsync(playerUuid, prefixed("honey.reload"));
        })
        .exceptionally(throwable -> {
          sendMessageAsync(playerUuid, prefixed("honey.reload.failed"));
          return null;
        });
    honey.getTranslationHandler().load();
    return Command.SINGLE_SUCCESS;
  }

  private static void sendMessageAsync(UUID playerUuid, Component message) {
    Bukkit.getGlobalRegionScheduler().run(Honey.getInstance(), scheduledTask -> {
      Player player = Bukkit.getPlayer(playerUuid);
      if (player != null) {
        player.getScheduler().run(Honey.getInstance(), task -> player.sendMessage(message), null);
      }
    });
  }

}

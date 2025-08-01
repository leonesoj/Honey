package io.github.leonesoj.honey.commands.messaging;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.SettingsController;
import io.github.leonesoj.honey.utils.other.OfflinePlayerUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.entity.Player;

public class UnignoreCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("unignore")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.messaging.ignore"))
        .then(Commands.argument("player", StringArgumentType.word())
            .executes(UnignoreCommand::commandUsage))
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    String target = ctx.getArgument("player", String.class);
    Player sender = (Player) ctx.getSource().getSender();

    OfflinePlayerUtil.getAsyncOfflinePlayer(target, offlinePlayer -> {

      SettingsController controller = Honey.getInstance().getDataHandler().getSettingsController();
      controller.getSettingsSync(sender.getUniqueId())
          .thenAccept(optional ->
              optional.ifPresent(settings -> {
                if (settings.getIgnoreList().contains(offlinePlayer.getUniqueId())) {
                  settings.getIgnoreList().remove(offlinePlayer.getUniqueId());

                  controller.updateSettingsSync(settings)
                      .thenAccept(success -> {
                        if (success) {
                          sender.sendMessage(Component.translatable("honey.settings.unignore.add",
                              Argument.component("player", Component.text(offlinePlayer.getName()))
                          ));
                        }
                      });
                } else {
                  sender.sendMessage(Component.translatable("honey.settings.unignore.missing"));
                }
              })
          );
    });
    return Command.SINGLE_SUCCESS;
  }

}

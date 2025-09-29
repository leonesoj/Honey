package io.github.leonesoj.honey.commands.messaging;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.SettingsController;
import io.github.leonesoj.honey.database.data.model.PlayerSettings;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.entity.Player;

public class IgnoreCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("ignore")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.messaging.ignore"))
        .then(Commands.argument("player", new OtherPlayerArgument(true))
            .executes(IgnoreCommand::commandUsage))
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    Player sender = (Player) ctx.getSource().getSender();
    Player target = ctx.getArgument("player", Player.class);

    SettingsController controller = Honey.getInstance().getDataHandler().getSettingsController();
    Optional<PlayerSettings> optional = controller.getSettings(sender.getUniqueId());
    if (optional.isPresent()) {
      PlayerSettings settings = optional.get();
      if (!settings.getIgnoreList().contains(target.getUniqueId())) {
        settings.addToIgnoreList(target.getUniqueId());

        controller.updateSettingsSync(settings)
            .thenAccept(result -> {
              if (result) {
                sender.sendMessage(Component.translatable("honey.settings.ignore.add",
                    Argument.component("player", Component.text(target.getName()))
                ));
              }
            });
      } else {
        sender.sendMessage(Component.translatable("honey.settings.ignore.already"));
      }

    }
    return Command.SINGLE_SUCCESS;
  }
}

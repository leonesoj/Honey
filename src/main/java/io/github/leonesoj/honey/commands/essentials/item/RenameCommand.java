package io.github.leonesoj.honey.commands.essentials.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.utils.command.MiniMessageArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RenameCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("rename")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.essentials.rename"))
        .then(Commands.argument("display_name", new MiniMessageArgument())
            .executes(RenameCommand::commandUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    Component displayName = ctx.getArgument("display_name", Component.class);

    ItemStack heldItem = sender.getInventory().getItemInMainHand();
    if (!heldItem.isEmpty()) {
      heldItem.editMeta(meta -> {
        if (!displayName.hasDecoration(TextDecoration.ITALIC)) {
          meta.customName(displayName.decoration(TextDecoration.ITALIC, false));
        } else {
          meta.customName(displayName);
        }
      });
    }
    return Command.SINGLE_SUCCESS;
  }

}

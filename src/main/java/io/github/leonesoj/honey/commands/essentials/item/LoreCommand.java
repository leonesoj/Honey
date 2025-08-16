package io.github.leonesoj.honey.commands.essentials.item;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.utils.command.MiniMessageArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LoreCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("lore")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.item.lore"))
        .then(Commands.literal("add")
            .then(Commands.argument("lore", new MiniMessageArgument())
                .executes(LoreCommand::addUsage)))
        .then(Commands.literal("remove")
            .then(Commands.argument("line_number", IntegerArgumentType.integer(1))
                .executes(LoreCommand::removeUsage)))
        .then(Commands.literal("set").then(
            Commands.argument("line_number", IntegerArgumentType.integer(1))
                .then(Commands.argument("lore", new MiniMessageArgument())
                    .executes(LoreCommand::setUsage))))
        .then(Commands.literal("clear").executes(LoreCommand::clearUsage))
        .build();
  }

  private static int addUsage(CommandContext<CommandSourceStack> ctx) {
    Component loreLine = ctx.getArgument("lore", Component.class);
    Player sender = (Player) ctx.getSource().getSender();

    ItemStack item = sender.getInventory().getItemInMainHand();

    List<Component> lore = getLore(item);
    lore.add(loreLine);
    item.lore(lore);

    sender.sendMessage(prefixed("honey.lore.add"));
    return Command.SINGLE_SUCCESS;
  }

  private static int setUsage(CommandContext<CommandSourceStack> ctx) {
    int lineNumber = ctx.getArgument("line_number", Integer.class) - 1;
    Component loreLine = ctx.getArgument("lore", Component.class);
    Player sender = (Player) ctx.getSource().getSender();

    ItemStack item = sender.getInventory().getItemInMainHand();

    List<Component> lore = getLore(item);
    if (lineNumber < lore.size()) {
      lore.set(lineNumber, loreLine);
      item.lore(lore);
      sender.sendMessage(prefixed("honey.lore.set",
          Argument.component("line_number", Component.text(lineNumber + 1))
      ));
    } else {
      sender.sendMessage(prefixed("honey.lore.failure"));
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int removeUsage(CommandContext<CommandSourceStack> ctx) {
    int lineNumber = ctx.getArgument("line_number", Integer.class) - 1;
    Player sender = (Player) ctx.getSource().getSender();

    ItemStack item = sender.getInventory().getItemInMainHand();

    List<Component> lore = getLore(item);
    if (lineNumber <= lore.size()) {
      lore.remove(lineNumber);
      sender.sendMessage(prefixed("honey.lore.remove",
          Argument.component("line_number", Component.text(lineNumber + 1))
      ));
    } else {
      sender.sendMessage(prefixed("honey.lore.failure"));
    }

    item.lore(lore);

    return Command.SINGLE_SUCCESS;
  }

  private static int clearUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    sender.getInventory().getItemInMainHand().lore(Collections.emptyList());
    sender.sendMessage(prefixed("honey.lore.clear"));
    return Command.SINGLE_SUCCESS;
  }

  private static List<Component> getLore(ItemStack item) {
    return item.getItemMeta().hasLore() ? item.lore() : new ArrayList<>();
  }

}

package io.github.leonesoj.honey.commands.essentials.item;

import static io.github.leonesoj.honey.locale.Message.argComponent;
import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MoreCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("more")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.essentials.more"))
        .executes(MoreCommand::commandUsage)
        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
            .executes(MoreCommand::amountUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> context) {
    Player sender = (Player) context.getSource().getSender();

    ItemStack itemStack = sender.getInventory().getItemInMainHand();
    itemStack.setAmount(itemStack.getMaxStackSize());
    sender.getInventory().setItemInMainHand(itemStack);

    sender.sendMessage(prefixed("honey.more"));
    return Command.SINGLE_SUCCESS;
  }

  private static int amountUsage(CommandContext<CommandSourceStack> context) {
    Player sender = (Player) context.getSource().getSender();
    int amount = IntegerArgumentType.getInteger(context, "amount");

    ItemStack itemStack = sender.getInventory().getItemInMainHand();
    itemStack.add(amount);
    sender.getInventory().setItemInMainHand(itemStack);
    sender.sendMessage(
        prefixed("honey.more.amount", argComponent("amount", amount))
    );
    return Command.SINGLE_SUCCESS;
  }

}

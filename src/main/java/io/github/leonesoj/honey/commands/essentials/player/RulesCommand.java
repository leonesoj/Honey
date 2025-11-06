package io.github.leonesoj.honey.commands.essentials.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.inventory.Book.Builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RulesCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("rules")
        .requires(stack -> stack.getSender() instanceof Player)
        .executes(RulesCommand::commandUsage)
        .build();
  }

  public static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    Player player = (Player) ctx.getSource().getSender();

    FileConfiguration config = Honey.getInstance().getTranslationHandler()
        .findBestTranslation("rulebook", player.locale());

    Builder rulebook = Book.builder();

    ConfigurationSection pages = config.getConfigurationSection("pages");
    pages.getKeys(false).forEach(key -> {
      Component pageContent = pages.getStringList(key).stream()
          .map(string -> MiniMessage.miniMessage().deserialize(string))
          .reduce((component, component2) -> {
            return component.append(Component.newline()).append(component2);
          }).get();

      rulebook.addPage(pageContent);
    });

    player.openBook(rulebook);
    return Command.SINGLE_SUCCESS;
  }

}

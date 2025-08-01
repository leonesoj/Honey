package io.github.leonesoj.honey.utils.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OtherPlayerArgument implements
    CustomArgumentType<Player, PlayerSelectorArgumentResolver> {

  private static final SimpleCommandExceptionType ERROR_BAD_SOURCE = new SimpleCommandExceptionType(
      MessageComponentSerializer.message()
          .serialize(Component.text("The source needs to be a CommandSourceStack!"))
  );

  private static final SimpleCommandExceptionType ERROR_NO_PLAYER = new SimpleCommandExceptionType(
      MessageComponentSerializer.message()
          .serialize(Component.text("The sender needs to be a Player"))
  );

  private static final SimpleCommandExceptionType ERROR_SELF = new SimpleCommandExceptionType(
      MessageComponentSerializer.message().serialize(
          Component.translatable("honey.command.target.self")
      )
  );

  @Override
  public Player parse(StringReader reader) {
    throw new UnsupportedOperationException("This method will never be called");
  }

  @Override
  public <S> Player parse(StringReader reader, S source) throws CommandSyntaxException {
    if (!(source instanceof CommandSourceStack stack)) {
      throw ERROR_BAD_SOURCE.create();
    }
    if (!(stack.getSender() instanceof Player sender)) {
      throw ERROR_NO_PLAYER.create();
    }

    Player player = getNativeType().parse(reader).resolve(stack).getFirst();
    if (sender.getUniqueId().equals(player.getUniqueId())) {
      throw ERROR_SELF.create();
    }

    return player;
  }

  @Override
  public ArgumentType<PlayerSelectorArgumentResolver> getNativeType() {
    return ArgumentTypes.player();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
      SuggestionsBuilder builder) {
    CommandSourceStack stack = (CommandSourceStack) context.getSource();
    UUID uuid = ((Player) stack.getSender()).getUniqueId();
    Bukkit.getOnlinePlayers().stream()
        .filter(player -> !player.getUniqueId().equals(uuid))
        .map(Player::getName)
        .forEach(builder::suggest);
    return builder.buildFuture();
  }
}

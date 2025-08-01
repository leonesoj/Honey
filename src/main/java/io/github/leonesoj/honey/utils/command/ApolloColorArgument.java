package io.github.leonesoj.honey.utils.command;

import com.lunarclient.apollo.common.ApolloColors;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import java.awt.Color;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;

public class ApolloColorArgument implements CustomArgumentType.Converted<Color, String> {

  @Override
  public Color convert(String color) throws CommandSyntaxException {

    return switch (color.toLowerCase(Locale.ROOT)) {
      case "black" -> ApolloColors.BLACK;
      case "dark_blue" -> ApolloColors.DARK_BLUE;
      case "dark_green" -> ApolloColors.DARK_GREEN;
      case "dark_aqua" -> ApolloColors.DARK_AQUA;
      case "dark_red" -> ApolloColors.DARK_RED;
      case "dark_purple" -> ApolloColors.DARK_PURPLE;
      case "gold" -> ApolloColors.GOLD;
      case "gray" -> ApolloColors.GRAY;
      case "dark_gray" -> ApolloColors.DARK_GRAY;
      case "blue" -> ApolloColors.BLUE;
      case "green" -> ApolloColors.GREEN;
      case "aqua" -> ApolloColors.AQUA;
      case "red" -> ApolloColors.RED;
      case "light_purple" -> ApolloColors.LIGHT_PURPLE;
      case "yellow" -> ApolloColors.YELLOW;
      case "white" -> ApolloColors.WHITE;
      default -> {
        Message message = MessageComponentSerializer.message()
            .serialize(Component.text(color + " is not a valid color."));
        throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
      }
    };
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.word();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
      SuggestionsBuilder builder) {
    builder.suggest("black");
    builder.suggest("dark_blue");
    builder.suggest("dark_green");
    builder.suggest("dark_aqua");
    builder.suggest("dark_red");
    builder.suggest("dark_purple");
    builder.suggest("gold");
    builder.suggest("gray");
    builder.suggest("dark_gray");
    builder.suggest("blue");
    builder.suggest("green");
    builder.suggest("aqua");
    builder.suggest("red");
    builder.suggest("light_purple");
    builder.suggest("yellow");
    return builder.buildFuture();
  }
}

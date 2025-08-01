package io.github.leonesoj.honey.utils.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.leonesoj.honey.utils.other.DurationUtil;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import java.time.Duration;
import net.kyori.adventure.text.Component;

public class DurationArgument implements CustomArgumentType.Converted<Duration, String> {

  private static final SimpleCommandExceptionType ERROR_INVALID_DURATION =
      new SimpleCommandExceptionType(MessageComponentSerializer.message()
          .serialize(Component.translatable("honey.command.duration.invalid")
          )
      );

  @Override
  public Duration convert(String nativeType) throws CommandSyntaxException {
    try {
      return DurationUtil.parse(nativeType);
    } catch (IllegalArgumentException exception) {
      throw ERROR_INVALID_DURATION.create();
    }
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.greedyString();
  }

}

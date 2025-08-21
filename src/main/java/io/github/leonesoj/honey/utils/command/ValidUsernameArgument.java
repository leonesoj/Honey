package io.github.leonesoj.honey.utils.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;

public class ValidUsernameArgument implements CustomArgumentType.Converted<String, String> {

  private static final SimpleCommandExceptionType ERROR_INVALID_USERNAME =
      new SimpleCommandExceptionType(MessageComponentSerializer.message()
          .serialize(Component.translatable("honey.command.username.invalid")
          )
      );

  @Override
  public String convert(String nativeType) throws CommandSyntaxException {
    int length = nativeType.length();
    if (length > 16) {
      throw ERROR_INVALID_USERNAME.create();
    }

    if (!nativeType.matches("^[A-Za-z0-9_]+$")) {
      throw ERROR_INVALID_USERNAME.create();
    }

    return nativeType;
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.word();
  }
}

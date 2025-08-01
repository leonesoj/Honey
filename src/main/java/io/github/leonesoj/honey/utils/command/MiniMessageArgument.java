package io.github.leonesoj.honey.utils.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MiniMessageArgument implements CustomArgumentType.Converted<Component, String> {

  @Override
  public Component convert(String nativeType) {
    return MiniMessage.miniMessage().deserialize(nativeType);
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.string();
  }
}

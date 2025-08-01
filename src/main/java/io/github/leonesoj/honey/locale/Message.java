package io.github.leonesoj.honey.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.jetbrains.annotations.NotNull;

public class Message {

  public static Component prefixed(String translationKey, ComponentLike... args) {
    TextComponent componentBuilder = Component.text()
        .append(Component.translatable("honey.prefix"))
        .append(Component.space())
        .append(Component.translatable(translationKey, args))
        .build();
    return componentBuilder.asComponent();
  }

  public static ComponentLike argComponent(@NotNull String name, Object value) {
    return Argument.component(name, Component.text(String.valueOf(value)));
  }

  public static String fancyStatus(boolean status) {
    return status ? "enabled" : "disabled";
  }

}

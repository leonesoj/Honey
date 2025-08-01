package io.github.leonesoj.honey.utils.other;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public class PlaceholderUtil {

  private PlaceholderUtil() {
  }

  public static Component applyPlaceholders(Player player, Component message) {
    if (DependCheck.isPlaceholderApiInstalled()) {
      return MiniMessage.miniMessage().deserialize(
          PlaceholderAPI.setPlaceholders(player,
              PlainTextComponentSerializer.plainText().serialize(message))
      );
    } else {
      return message;
    }
  }

}

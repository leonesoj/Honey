package io.github.leonesoj.honey.chat;

import io.github.leonesoj.honey.utils.other.DependCheck;
import java.util.Optional;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class HoneyChatRenderer {

  private static final String DISPLAY_NAME_PLACEHOLDER = "display_name";
  private static final String USERNAME_PLACEHOLDER = "username";
  private static final String CHAT_MESSAGE_PLACEHOLDER = "message";
  private static final String PREFIX_PLACEHOLDER = "prefix";
  private static final String SUFFIX_PLACEHOLDER = "suffix";
  private static final String PRIMARY_GROUP_PLACEHOLDER = "group";

  private static final MiniMessage miniMessage = MiniMessage.miniMessage();

  private static HoneyChatRenderer instance;

  public static HoneyChatRenderer getInstance() {
    if (instance == null) {
      instance = new HoneyChatRenderer();
    }
    return instance;
  }

  private HoneyChatRenderer() {
  }

  public Component render(Player source, Component sourceDisplayName, Component message,
      Audience viewer, SignedMessage signedMessage, ChatService chatService) {
    Component base = Component.empty();

    ChatChannel sourceChannel = chatService.getMemberChannel(source);
    if (viewer instanceof ConsoleCommandSender) {
      base = base.append(Component.text("(" + sourceChannel.getIdentifier() + ") "));
    }

    Optional<UUID> viewerUuid = viewer.get(Identity.UUID);
    if (viewerUuid.isPresent() && chatService.isChatMod(viewerUuid.get())) {
      Component deleteButton = Component.textOfChildren(
          Component.text("[", NamedTextColor.DARK_GRAY),
          Component.text("✖", NamedTextColor.RED).clickEvent(
              ClickEvent.callback(audience -> Bukkit.getServer().deleteMessage(signedMessage))
          ),
          Component.text("]", NamedTextColor.DARK_GRAY),
          Component.space()
      );
      base = base.append(deleteButton);
    }

    String format = sourceChannel.getFormat();
    if (DependCheck.isPlaceholderApiInstalled()) {
      format = PlaceholderAPI.setPlaceholders(source, format);
    }

    CachedMetaData metaData = LuckPermsProvider.get()
        .getPlayerAdapter(Player.class)
        .getMetaData(source);
    Component body = MiniMessage.miniMessage().deserialize(format,
        Placeholder.component(DISPLAY_NAME_PLACEHOLDER, sourceDisplayName),
        Placeholder.component(USERNAME_PLACEHOLDER, Component.text(source.getName())),
        Placeholder.component(CHAT_MESSAGE_PLACEHOLDER, message),
        Placeholder.component(PREFIX_PLACEHOLDER, componentOrEmpty(metaData.getPrefix())),
        Placeholder.component(SUFFIX_PLACEHOLDER, componentOrEmpty(metaData.getSuffix()))
    );
    base = base.append(body);

    return base;
  }

  private Component componentOrEmpty(String string) {
    return string != null ? miniMessage.deserialize(string) : Component.empty();
  }

}

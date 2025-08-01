package io.github.leonesoj.honey.chat;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.utils.other.DependCheck;
import io.github.leonesoj.honey.utils.other.PlaceholderUtil;
import io.papermc.paper.chat.ChatRenderer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class HoneyChatRenderer implements ChatRenderer {

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

  @Override
  public Component render(Player source, Component sourceDisplayName, Component message,
      Audience viewer) {
    ChatService chatService = Honey.getInstance().getChatService();
    ChatChannel sourceChannel = chatService.getMemberChannel(source);

    if (DependCheck.isPlaceholderApiInstalled()) {
      message = PlaceholderUtil.applyPlaceholders(source, message);
    }

    TagResolver prefix = TagResolver.empty();
    TagResolver suffix = TagResolver.empty();
    TagResolver group = TagResolver.empty();
    if (DependCheck.isVaultInstalled() && chatService.getVaultChat() != null) {
      Chat chat = chatService.getVaultChat();
      prefix = Placeholder.component(PREFIX_PLACEHOLDER,
          miniMessage.deserialize(chat.getPlayerPrefix(source))
      );
      suffix = Placeholder.component(SUFFIX_PLACEHOLDER,
          miniMessage.deserialize(chat.getPlayerSuffix(source))
      );
      group = Placeholder.component(PRIMARY_GROUP_PLACEHOLDER,
          miniMessage.deserialize(chat.getPrimaryGroup(source))
      );
    }

    Component result = MiniMessage.miniMessage().deserialize(sourceChannel.getFormat(),
        Placeholder.component(DISPLAY_NAME_PLACEHOLDER, sourceDisplayName),
        Placeholder.component(USERNAME_PLACEHOLDER, Component.text(source.getName())),
        Placeholder.component(CHAT_MESSAGE_PLACEHOLDER, message),
        prefix,
        suffix,
        group
    );

    Component channelPrefix = Component.empty();
    if (viewer instanceof ConsoleCommandSender) {
      channelPrefix = Component.text("(" + sourceChannel.getIdentifier() + ") ");
    }

    return channelPrefix.append(result);
  }

}

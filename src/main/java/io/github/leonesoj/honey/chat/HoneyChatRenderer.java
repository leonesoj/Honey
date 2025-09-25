package io.github.leonesoj.honey.chat;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.utils.other.DependCheck;
import io.github.leonesoj.honey.utils.other.PlaceholderUtil;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
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

    CachedMetaData metaData = LuckPermsProvider.get()
        .getPlayerAdapter(Player.class)
        .getMetaData(source);

    Component result = MiniMessage.miniMessage().deserialize(sourceChannel.getFormat(),
        Placeholder.component(DISPLAY_NAME_PLACEHOLDER, sourceDisplayName),
        Placeholder.component(USERNAME_PLACEHOLDER, Component.text(source.getName())),
        Placeholder.component(CHAT_MESSAGE_PLACEHOLDER, message),
        Placeholder.component(PREFIX_PLACEHOLDER, componentOrEmpty(metaData.getPrefix())),
        Placeholder.component(SUFFIX_PLACEHOLDER, componentOrEmpty(metaData.getSuffix()))
    );

    Component channelPrefix = Component.empty();
    if (viewer instanceof ConsoleCommandSender) {
      channelPrefix = Component.text("(" + sourceChannel.getIdentifier() + ") ");
    }

    return channelPrefix.append(result);
  }

  private Component componentOrEmpty(String string) {
    return string != null ? miniMessage.deserialize(string) : Component.empty();
  }

}

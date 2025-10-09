package io.github.leonesoj.honey.chat;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.filtering.ChatFilter.Result;
import io.github.leonesoj.honey.chat.variables.ItemVariable;
import io.github.leonesoj.honey.chat.variables.LocationVariable;
import io.github.leonesoj.honey.chat.variables.PingVariable;
import io.github.leonesoj.honey.chat.variables.VariableRegistry;
import io.github.leonesoj.honey.config.Config;
import io.github.leonesoj.honey.database.data.model.PlayerSettings;
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

  private static final VariableRegistry VARIABLE_REGISTRY = new VariableRegistry()
      .register(new ItemVariable())
      .register(new LocationVariable())
      .register(new PingVariable());

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
      Audience viewer, SignedMessage signedMessage, Result chatResult, ChatService chatService) {
    Component base = Component.empty();

    ChatChannel sourceChannel = chatService.getMemberChannel(source);
    if (viewer instanceof ConsoleCommandSender) {
      base = base.append(Component.text("(" + sourceChannel.getIdentifier() + ") "));
    }

    Optional<UUID> optional = viewer.get(Identity.UUID);
    if (optional.isPresent()) {
      UUID viewerUuid = optional.get();

      if (chatService.isChatMod(viewerUuid)) {
        Component deleteButton = Component.textOfChildren(
            Component.text("[", NamedTextColor.DARK_GRAY),
            Component.text("✖", NamedTextColor.RED).clickEvent(
                ClickEvent.callback(audience -> Bukkit.getServer().deleteMessage(signedMessage))
            ),
            Component.text("]", NamedTextColor.DARK_GRAY),
            Component.space()
        );
        base = base.append(deleteButton);
      } else {
        Optional<PlayerSettings> viewerSettings = Honey.getInstance().getDataHandler()
            .getSettingsController().getSettings(viewerUuid);
        if (viewerSettings.isPresent() && viewerSettings.get().hasProfanityFilter()) {
          message = chatResult.censoredText();
        }
      }

    }

    String format = sourceChannel.getFormat();
    if (DependCheck.isPlaceholderApiInstalled()) {
      format = PlaceholderAPI.setPlaceholders(source, format);
    }

    Config config = Honey.getInstance().config();
    if (config.getBoolean("chat.variables.enabled")) {
      message = VARIABLE_REGISTRY.applyAll(message, source,
          config.getBoolean("chat.variables.require_permission")
      );
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

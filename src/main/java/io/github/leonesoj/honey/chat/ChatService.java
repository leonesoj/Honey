package io.github.leonesoj.honey.chat;

import static io.github.leonesoj.honey.locale.Message.argComponent;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.messaging.PrivateChatService;
import io.github.leonesoj.honey.utils.other.DurationUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatService implements Listener {

  private final ConcurrentHashMap<String, ChatChannel> channels = new ConcurrentHashMap<>();

  private final Set<UUID> chatMods = ConcurrentHashMap.newKeySet();

  private final PrivateChatService privateChatService;
  private final SpyService spyService;

  private ChatChannel defaultChannel;

  public ChatService(SpyService spyService) {
    this.spyService = spyService;
    this.privateChatService = new PrivateChatService(spyService);
    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
  }

  public void registerChannel(ChatChannel chatChannel) {
    if (channels.containsKey(chatChannel.getIdentifier())) {
      throw new IllegalArgumentException(
          "Chat channel already registered with id: " + chatChannel.getIdentifier());
    }

    channels.put(chatChannel.getIdentifier(), chatChannel);
  }

  public void setDefaultChannel(ChatChannel defaultChannel) {
    if (!channels.containsKey(defaultChannel.getIdentifier())) {
      throw new IllegalArgumentException("Default channel must be already registered");
    }
    this.defaultChannel = defaultChannel;
  }

  public void joinChannel(String channelName, Audience audience) {
    if (!channels.containsKey(channelName)) {
      throw new IllegalArgumentException("Channel " + channelName + " does not exist");
    }

    ChatChannel oldChannel = getMemberChannel(audience);
    if (oldChannel != null) {
      leaveChannel(oldChannel.getIdentifier(), audience);
    }

    ChatChannel newChannel = channels.get(channelName);
    if (newChannel.canJoin(audience)) {
      newChannel.addParticipant(audience);
    }

  }

  public boolean changeChannel(ChatChannel targetChannel, Audience audience) {
    ChatChannel current = getParticipantChannel(audience);

    if (!channels.containsKey(targetChannel.getIdentifier())) {
      throw new IllegalArgumentException(
          "Channel " + targetChannel.getIdentifier() + " does not exist");
    }

    if (current != null && current != targetChannel) {
      // Leave current as participant, stay as listener
      current.removeParticipant(audience); // leave general participants
      if (current.equals(defaultChannel)) {
        current.addListener(audience);
      }
    }

    if (!isSpy(audience)) {
      // Remove from listeners in the target channel to avoid dual membership
      targetChannel.removeListener(audience);
    }

    if (targetChannel.canJoin(audience)) {
      targetChannel.addParticipant(audience);
      return true;
    }

    return false;
  }

  public void leaveChannel(String channelId, Audience audience) {
    if (isSpy(audience)) {
      return;
    }

    ChatChannel channel = channels.get(channelId);

    if (channel == null) {
      throw new IllegalArgumentException("Channel " + channelId + " does not exist");
    }

    channel.removeParticipant(audience);
    channel.removeListener(audience);

    if (getParticipantChannel(audience) == null && defaultChannel != null) {
      if (defaultChannel.canJoin(audience)) {
        defaultChannel.addParticipant(audience);
      }
    }
  }

  public boolean isSpy(Audience audience) {
    Optional<UUID> optional = audience.get(Identity.UUID);
    return optional.isPresent() && spyService.isGlobalSpy(optional.get());
  }

  public void setChatModStatus(UUID uuid, boolean status) {
    if (status) {
      chatMods.add(uuid);
    } else {
      chatMods.remove(uuid);
    }

    Honey.getInstance().getDataHandler().getStaffSettingsController()
        .modifySettings(uuid, settings -> {
          settings.setChatModeration(status);
          return settings;
        });
  }

  public boolean isChatMod(UUID uuid) {
    return chatMods.contains(uuid);
  }

  public ChatChannel getMemberChannel(Audience audience) {
    for (ChatChannel channel : channels.values()) {
      if (channel.hasParticipant(audience)) {
        return channel;
      }
    }

    return null;
  }

  public ChatChannel getParticipantChannel(Audience audience) {
    for (ChatChannel channel : channels.values()) {
      if (channel.hasParticipant(audience)) {
        return channel;
      }
    }
    return null;
  }

  public Collection<ChatChannel> getChannels() {
    return channels.values();
  }

  public ChatChannel getChannel(String channelName) {
    return channels.get(channelName);
  }

  public PrivateChatService getPrivateChatService() {
    return privateChatService;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    channels.values().forEach(chatChannel -> {
      if (chatChannel.shouldDefaultJoin() && chatChannel.canJoin(event.getPlayer())) {
        joinChannel(chatChannel.getIdentifier(), event.getPlayer());
      }
    });
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    channels.values().forEach(chatChannel -> {
      if (chatChannel.hasParticipant(event.getPlayer())) {
        leaveChannel(chatChannel.getIdentifier(), event.getPlayer());
      }
    });
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncChatEvent event) {
    Player player = event.getPlayer();
    ChatChannel chatChannel = getMemberChannel(player);
    if (chatChannel == null) {
      event.setCancelled(true);
      return;
    }

    if (!chatChannel.canTalk(player)) {
      player.sendMessage(Component.translatable("honey.channel.disallowed.talk"));
      event.setCancelled(true);
      return;
    }

    if (!chatChannel.canMuteTalk(player) && chatChannel.isMuted()) {
      player.sendMessage(Component.translatable("honey.channel.disallowed.mute"));
      event.setCancelled(true);
      return;
    }

    if (chatChannel.isSlowed() && !chatChannel.canSlowTalk(player)) {
      if (chatChannel.isParticipantSlowed(player.getUniqueId())) {
        player.sendMessage(Component.translatable("honey.channel.disallowed.slow",
            argComponent("duration",
                DurationUtil.format(chatChannel.getRemaining(player.getUniqueId())))
        ));
        event.setCancelled(true);
        return;
      } else {
        chatChannel.slowParticipant(player.getUniqueId());
      }
    }

    event.viewers().removeIf(audience ->
        !chatChannel.hasMember(audience) && !isSpy(audience)
    );
    event.renderer((source, sourceDisplayName, message, viewer) ->
        HoneyChatRenderer.getInstance()
            .render(source, sourceDisplayName, message, viewer, event.signedMessage(), this)
    );
  }
}

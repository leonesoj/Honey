package io.github.leonesoj.honey.chat;

import static io.github.leonesoj.honey.locale.Message.argComponent;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.MessageGuard.CheckResult;
import io.github.leonesoj.honey.chat.filtering.ChatFilter;
import io.github.leonesoj.honey.chat.filtering.ChatFilter.Result;
import io.github.leonesoj.honey.chat.filtering.ForbiddenWords;
import io.github.leonesoj.honey.chat.messaging.PrivateChatService;
import io.github.leonesoj.honey.database.data.model.PlayerSettings;
import io.github.leonesoj.honey.utils.other.DurationUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatService implements Listener {

  private static final Duration CHAT_COOLDOWN = Duration.ofSeconds(3);
  private static final int MAX_CHAT_DUPE = 3;

  private final ConcurrentHashMap<String, ChatChannel> channels = new ConcurrentHashMap<>();

  private final Set<UUID> chatMods = ConcurrentHashMap.newKeySet();

  private final PrivateChatService privateChatService;
  private final SpyService spyService;

  private final ChatFilter filter;
  private final MessageGuard messageGuard = new MessageGuard(CHAT_COOLDOWN, MAX_CHAT_DUPE);

  private ChatChannel defaultChannel;

  public ChatService() {
    this.spyService = new SpyService();
    this.filter = new ChatFilter(ForbiddenWords.load(Honey.getInstance()));
    this.privateChatService = new PrivateChatService(spyService, filter);

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

  public SpyService getSpyService() {
    return spyService;
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

    Optional<PlayerSettings> senderSettingsOpt = Honey.getInstance()
        .getDataHandler()
        .getSettingsController()
        .getSettings(player.getUniqueId());

    if (senderSettingsOpt.isPresent() && !senderSettingsOpt.get().hasChatMessages()) {
      player.sendMessage(Component.translatable("honey.channel.disallowed.settings"));
      event.setCancelled(true);
      return;
    }

    String raw = event.signedMessage().message();

    CheckResult guard = messageGuard.check(player.getUniqueId(), raw);
    switch (guard.violation()) {
      case COOLDOWN -> {
        if (!player.hasPermission("honey.chat.bypass.cooldown")) {
          double secs = guard.remainingMillis() / 1000.0;
          String pretty = String.format(java.util.Locale.US, "%.2f", secs);
          player.sendMessage(Component.translatable(
              "honey.channel.disallowed.cooldown",
              argComponent("duration", pretty)
          ));
          event.setCancelled(true);
          return;
        }
      }
      case DUPLICATE -> {
        if (!player.hasPermission("honey.chat.bypass.duplicate")) {
          player.sendMessage(Component.translatable("honey.channel.disallowed.duplicate"));
          event.setCancelled(true);
          return;
        }
      }
    }

    Result result = filter.apply(raw);

    switch (result.action()) {
      case BLOCK_BAD_CHAR -> {
        player.sendMessage(Component.translatable("honey.channel.disallowed.badchar"));
        event.setCancelled(true);
        return;
      }
      case BLOCK_URL -> {
        player.sendMessage(Component.translatable("honey.channel.disallowed.url"));
        event.setCancelled(true);
        return;
      }
      case BLOCK_IP -> {
        player.sendMessage(Component.translatable("honey.channel.disallowed.ip"));
        event.setCancelled(true);
        return;
      }
      case BLOCK_WORD -> {
        player.sendMessage(Component.translatable("honey.channel.disallowed.forbidden"));
        event.setCancelled(true);
        return;
      }
      case CENSOR, ALLOW -> messageGuard.recordSuccess(player.getUniqueId(), raw);
    }

    event.viewers().removeIf(audience ->
        !chatChannel.hasMember(audience) && !isSpy(audience)
    );

    for (Iterator<Audience> it = event.viewers().iterator(); it.hasNext(); ) {
      Audience audience = it.next();

      Optional<UUID> optional = audience.get(Identity.UUID);
      if (optional.isEmpty()) {
        continue;
      }

      UUID uuid = optional.get();

      Optional<PlayerSettings> settingsOpt = Honey.getInstance()
          .getDataHandler()
          .getSettingsController()
          .getSettings(uuid);

      if (settingsOpt.isEmpty()) {
        continue;
      }

      PlayerSettings settings = settingsOpt.get();

      if (!settings.hasChatMessages()) {
        it.remove();
        continue;
      }

      if (settings.hasSoundAlerts()) {
        Player viewer = Bukkit.getPlayer(uuid);
        if (viewer != null) {
          String name = viewer.getName().toLowerCase(Locale.ROOT);
          String plainMessage = event.signedMessage().message().toLowerCase(Locale.ROOT);
          if (plainMessage.contains(name)) {
            viewer.playSound(viewer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
          }
        }
      }
    }

    event.renderer((source, sourceDisplayName, message, viewer) ->
        HoneyChatRenderer.getInstance()
            .render(source, sourceDisplayName, message, viewer, event.signedMessage(), result, this)
    );
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
    chatMods.remove(event.getPlayer().getUniqueId());
    messageGuard.clear(event.getPlayer().getUniqueId());
    channels.values().forEach(chatChannel -> {
      if (chatChannel.hasParticipant(event.getPlayer())) {
        leaveChannel(chatChannel.getIdentifier(), event.getPlayer());
      }
    });
  }

}

package io.github.leonesoj.honey.chat.messaging;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.SpyService;
import io.github.leonesoj.honey.database.data.controller.SettingsController;
import io.github.leonesoj.honey.database.data.model.PlayerSettings;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class PrivateChatService implements Listener {

  private final Map<ChatPair, ChatSession> activeSessions = new HashMap<>();
  private final Map<UUID, UUID> lastContacts = new HashMap<>();

  private final SpyService spyService;

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  private static final String PRIVATE_MESSAGE_FORMAT_PATH = "private_messaging.format.";

  public PrivateChatService(SpyService spyService) {
    this.spyService = spyService;
  }

  private void startSession(UUID participantA, UUID participantB) {
    ChatPair pair = new ChatPair(participantA, participantB);
    activeSessions.computeIfAbsent(pair, key -> new ChatSession(participantA, participantB));
  }

  private void respond(UUID sender, UUID recipient) {
    ChatPair pair = new ChatPair(sender, recipient);
    activeSessions.computeIfAbsent(pair, key -> new ChatSession(sender, recipient))
        .recordMessage(sender);
  }

  public void sendPrivateMessage(Player sender, Player recipient, Component message) {
    UUID senderUuid = sender.getUniqueId();
    UUID recipientUuid = recipient.getUniqueId();

    SettingsController settingsController = Honey.getInstance().getDataHandler()
        .getSettingsController();

    CompletableFuture<Optional<PlayerSettings>> senderSettingsFuture =
        settingsController.getSettings(senderUuid);

    CompletableFuture<Optional<PlayerSettings>> targetSettingsFuture =
        settingsController.getSettings(recipientUuid);

    senderSettingsFuture.thenCombine(targetSettingsFuture,
        (senderSettingsOpt, targetSettingsOpt) -> {
          // Proceed only if both are present
          if (senderSettingsOpt.isEmpty() || targetSettingsOpt.isEmpty()) {
            return null;
          }

          Bukkit.getGlobalRegionScheduler().run(Honey.getInstance(), task -> {
            PlayerSettings senderSettings = senderSettingsOpt.get();
            PlayerSettings targetSettings = targetSettingsOpt.get();

            if (!senderSettings.hasPrivateMessages()) {
              sender.sendMessage(Component.translatable("honey.messaging.self.disallowed"));
              return;
            }

            if (!targetSettings.hasPrivateMessages()) {
              sender.sendMessage(Component.translatable("honey.messaging.target.disallowed"));
              return;
            }

            if (senderSettings.getIgnoreList().contains(recipientUuid)) {
              sender.sendMessage(Component.translatable("honey.messaging.target.ignored"));
              return;
            }

            if (targetSettings.getIgnoreList().contains(senderUuid)) {
              sender.sendMessage(Component.translatable("honey.messaging.self.ignored"));
              return;
            }

            if (targetSettings.hasSoundAlerts()) {
              recipient.playSound(recipient, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 0.5F);
            }

            startSession(senderUuid, recipientUuid);

            lastContacts.put(senderUuid, recipientUuid);
            lastContacts.putIfAbsent(recipientUuid, senderUuid);

            respond(senderUuid, recipientUuid); // For metadata tracking

            ConfigurationSection chatSection = Honey.getInstance().getConfig()
                .getConfigurationSection("chat");
            Component toUser = MINI_MESSAGE.deserialize(
                chatSection.getString(PRIVATE_MESSAGE_FORMAT_PATH + "to_user"),
                Placeholder.component("recipient", recipient.displayName()),
                Placeholder.component("message", message)
            );
            Component fromUser = MINI_MESSAGE.deserialize(
                chatSection.getString(PRIVATE_MESSAGE_FORMAT_PATH + "from_user"),
                Placeholder.component("sender", sender.displayName()),
                Placeholder.component("message", message)
            );

            sender.sendMessage(toUser);
            recipient.sendMessage(fromUser);

            if (spyService.isBeingSpied(recipientUuid) || spyService.isBeingSpied(senderUuid)) {
              TagResolver playerPlaceholder = Placeholder.component("player", sender.name());
              TagResolver recipientPlaceholder = Placeholder.component("recipient",
                  recipient.name());
              TagResolver messagePlaceholder = Placeholder.component("message", message);

              Component toSpy = MINI_MESSAGE.deserialize(
                  chatSection.getString("spy.format.private_message"),
                  playerPlaceholder,
                  recipientPlaceholder,
                  messagePlaceholder
              );

              Set<UUID> spies = new HashSet<>();
              spies.addAll(spyService.getSpiesBySubject(senderUuid));
              spies.addAll(spyService.getSpiesBySubject(recipientUuid));

              for (UUID spy : spies) {
                Player spyPlayer = Bukkit.getPlayer(spy);
                if (spyPlayer != null && spyPlayer.isOnline()) {
                  spyPlayer.sendMessage(toSpy);
                }
              }
            }
          });

          return null;
        });
  }

  public UUID getLastContact(UUID sender) {
    return lastContacts.get(sender);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID quitting = event.getPlayer().getUniqueId();

    // Remove all sessions involving this player
    activeSessions.entrySet().removeIf(entry -> entry.getKey().involves(quitting));
    lastContacts.remove(quitting);
  }
}


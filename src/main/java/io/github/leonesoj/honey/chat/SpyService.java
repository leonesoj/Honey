package io.github.leonesoj.honey.chat;

import static io.github.leonesoj.honey.locale.Message.prefixed;

import io.github.leonesoj.honey.Honey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpyService implements Listener {

  private final Set<UUID> globalSpies = ConcurrentHashMap.newKeySet();
  private final Map<UUID, Set<UUID>> spySubjects = new HashMap<>();

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  public SpyService() {
    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
  }

  public boolean toggleGlobalSpy(UUID uuid) {
    if (globalSpies.contains(uuid)) {
      globalSpies.remove(uuid);
      return false;
    } else {
      globalSpies.add(uuid);
      return true;
    }
  }

  public void trackAsSpy(UUID spy, UUID subject) {
    spySubjects.computeIfAbsent(spy, k -> ConcurrentHashMap.newKeySet()).add(subject);
  }

  public boolean isGlobalSpy(UUID uuid) {
    return globalSpies.contains(uuid);
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();

    Set<UUID> spies = getSpiesBySubject(player.getUniqueId());

    if (!spies.isEmpty()) {
      String commandNotification = Honey.getInstance().getConfigHandler().getMainConfig()
          .getString("chat.spy.format.command");

      Component msg = MINI_MESSAGE.deserialize(commandNotification,
          Placeholder.component("player", player.name()),
          Placeholder.component("command", Component.text(event.getMessage()))
      );

      for (UUID spy : spies) {
        Player spyPlayer = Bukkit.getPlayer(spy);
        if (spyPlayer != null && spyPlayer.isOnline()) {
          spyPlayer.sendMessage(msg);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent event) {
    UUID playerUuid = event.getPlayer().getUniqueId();
    spySubjects.remove(playerUuid);

    for (Map.Entry<UUID, Set<UUID>> entry : spySubjects.entrySet()) {
      UUID spy = entry.getKey();
      Set<UUID> subjects = entry.getValue();

      if (subjects.remove(playerUuid)) {
        Bukkit.getPlayer(spy).sendMessage(prefixed("honey.spy.subject.offline"));
      }
    }
  }

  public Set<UUID> getSpiesBySubject(UUID subject) {
    return spySubjects.entrySet().stream()
        .filter(entry -> entry.getValue().contains(subject))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  public boolean isBeingSpied(UUID subject) {
    return spySubjects.values().stream().anyMatch(set -> set.contains(subject));
  }

  public Set<UUID> getTargetsOfSpy(UUID spy) {
    return spySubjects.getOrDefault(spy, Collections.emptySet());
  }

  public void untrackSpyTarget(UUID spy, UUID target) {
    Set<UUID> targets = spySubjects.get(spy);
    if (targets != null) {
      targets.remove(target);
      if (targets.isEmpty()) {
        spySubjects.remove(spy);
      }
    }
  }

}

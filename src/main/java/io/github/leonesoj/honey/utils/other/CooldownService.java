package io.github.leonesoj.honey.utils.other;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class CooldownService implements Listener {

  private final Map<UUID, CooldownData> cooldownPlayers = new HashMap<>();

  public void addPlayer(UUID uuid, long cooldown) {
    cooldownPlayers.put(uuid, new CooldownData(System.currentTimeMillis(), cooldown));
  }

  public boolean hasCooldown(UUID uuid) {
    CooldownData cooldownData = cooldownPlayers.get(uuid);
    if (cooldownData == null) {
      return false;
    }

    long elapsed = System.currentTimeMillis() - cooldownData.startTimeMillis();
    boolean stillOnCooldown = elapsed < cooldownData.cooldownMillis();

    if (!stillOnCooldown) {
      cooldownPlayers.remove(uuid);
    }

    return stillOnCooldown;
  }

  public Duration getRemaining(UUID uuid) {
    CooldownData cooldownData = cooldownPlayers.get(uuid);
    if (cooldownData == null) {
      return Duration.ZERO;
    }

    long elapsed = System.currentTimeMillis() - cooldownData.startTimeMillis();
    long remainingMillis = cooldownData.cooldownMillis() - elapsed;
    return Duration.ofMillis(Math.max(remainingMillis, 0));
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent event) {
    cooldownPlayers.remove(event.getPlayer().getUniqueId());
  }

  private record CooldownData(long startTimeMillis, long cooldownMillis) {
  }

}

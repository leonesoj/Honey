package io.github.leonesoj.honey.utils.other;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownService {

  private final Map<UUID, Long> cooldownPlayers = new HashMap<>();
  private final long cooldownMillis;

  public CooldownService(long cooldownSeconds) {
    this.cooldownMillis = cooldownSeconds * 1000;
  }

  public void addPlayer(UUID uuid) {
    cooldownPlayers.put(uuid, System.currentTimeMillis());
  }

  public boolean hasCooldown(UUID uuid) {
    Long startTime = cooldownPlayers.get(uuid);
    if (startTime == null) {
      return false;
    }

    long elapsed = System.currentTimeMillis() - startTime;
    return elapsed < cooldownMillis;
  }

  public Duration getRemaining(UUID uuid) {
    Long startTime = cooldownPlayers.get(uuid);
    if (startTime == null) {
      return Duration.ZERO;
    }

    long remainingMillis = cooldownMillis - (System.currentTimeMillis() - startTime);
    return Duration.ofMillis(Math.max(remainingMillis, 0));
  }

  public void remove(UUID uuid) {
    cooldownPlayers.remove(uuid);
  }
}

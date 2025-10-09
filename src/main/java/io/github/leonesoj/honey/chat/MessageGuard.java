package io.github.leonesoj.honey.chat;

import java.time.Duration;
import java.util.Deque;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class MessageGuard {

  private final int historySize;
  private final long cooldownMillis;

  private final ConcurrentHashMap<UUID, Deque<String>> history = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, Long> lastSent = new ConcurrentHashMap<>();

  public MessageGuard(Duration cooldown, int historySize) {
    this.cooldownMillis = Math.max(0, cooldown.toMillis());
    this.historySize = Math.max(1, historySize);
  }

  public enum Violation {
    NONE, DUPLICATE, COOLDOWN
  }

  public static final class CheckResult {

    private final Violation violation;
    private final long remainingMillis;

    CheckResult(Violation v, long rem) {
      this.violation = v;
      this.remainingMillis = rem;
    }

    public Violation violation() {
      return violation;
    }

    public long remainingMillis() {
      return remainingMillis;
    }
  }

  public CheckResult check(UUID playerId, String messageRaw) {
    final long now = System.currentTimeMillis();

    Long last = lastSent.get(playerId);
    if (last != null) {
      long elapsed = now - last;
      if (elapsed < cooldownMillis) {
        return new CheckResult(Violation.COOLDOWN, cooldownMillis - elapsed);
      }
    }

    String normalized = normalize(messageRaw);
    if (!normalized.isEmpty()) {
      Deque<String> q = history.get(playerId);
      if (q != null && q.contains(normalized)) {
        return new CheckResult(Violation.DUPLICATE, 0L);
      }
    }

    return new CheckResult(Violation.NONE, 0L);
  }

  public void recordSuccess(UUID playerId, String message) {
    lastSent.put(playerId, System.currentTimeMillis());

    Deque<String> q = history.computeIfAbsent(playerId, id -> new ConcurrentLinkedDeque<>());
    while (q.size() >= historySize) {
      q.pollFirst();
    }
    message = normalize(message);
    if (!message.isEmpty()) {
      q.offerLast(message);
    }
  }

  public void clear(UUID playerId) {
    history.remove(playerId);
    lastSent.remove(playerId);
  }

  private String normalize(String s) {
    if (s == null) {
      return "";
    }
    String t = s
        .toLowerCase(Locale.ROOT)
        .replaceAll("\\p{Cntrl}", "")
        .replaceAll("[\\p{Z}\\s]+", " ")
        .trim();
    t = t.replaceAll("([!?.])\\1{1,}", "$1");
    return t;
  }
}

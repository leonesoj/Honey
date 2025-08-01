package io.github.leonesoj.honey.chat.messaging;

import java.util.UUID;

public record ChatPair(UUID a, UUID b) {

  public ChatPair {
    // Ensure consistent ordering for symmetry
    if (a.compareTo(b) > 0) {
      UUID tmp = a;
      a = b;
      b = tmp;
    }
  }

  public boolean involves(UUID uuid) {
    return a.equals(uuid) || b.equals(uuid);
  }

  public UUID getOther(UUID uuid) {
    if (uuid.equals(a)) {
      return b;
    }
    if (uuid.equals(b)) {
      return a;
    }
    throw new IllegalArgumentException("UUID not part of ChatPair");
  }
}

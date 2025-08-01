package io.github.leonesoj.honey.chat.messaging;

import java.util.UUID;

public class ChatSession {

  private final UUID a;
  private final UUID b;

  public ChatSession(UUID a, UUID b) {
    this.a = a;
    this.b = b;
  }

  public boolean involves(UUID uuid) {
    return uuid.equals(a) || uuid.equals(b);
  }

  public UUID getOther(UUID uuid) {
    if (uuid.equals(a)) return b;
    if (uuid.equals(b)) return a;
    throw new IllegalArgumentException("UUID not part of this session.");
  }

  public void recordMessage(UUID sender) {
    // Optional metadata tracking
  }
}


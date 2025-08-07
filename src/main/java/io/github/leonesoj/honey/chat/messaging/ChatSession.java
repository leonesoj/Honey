package io.github.leonesoj.honey.chat.messaging;

import java.util.UUID;

public class ChatSession {

  private final UUID participantA;
  private final UUID participantB;

  public ChatSession(UUID participantA, UUID participantB) {
    this.participantA = participantA;
    this.participantB = participantB;
  }

  public boolean involves(UUID uuid) {
    return uuid.equals(participantA) || uuid.equals(participantB);
  }

  public UUID getOther(UUID uuid) {
    if (uuid.equals(participantA)) {
      return participantB;
    }
    if (uuid.equals(participantB)) {
      return participantA;
    }
    throw new IllegalArgumentException("UUID not part of this session.");
  }

  public void recordMessage(UUID sender) {
    // Optional metadata tracking
  }
}


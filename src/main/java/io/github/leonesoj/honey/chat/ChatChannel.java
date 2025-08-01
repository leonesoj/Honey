package io.github.leonesoj.honey.chat;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Bukkit;

public class ChatChannel {

  /** Duration in milliseconds. */
  private static final long DEFAULT_SLOW_DURATION = 5000L;

  private final String identifier;
  private String format;

  private final Set<Audience> participants = new CopyOnWriteArraySet<>();
  private final Set<Audience> listeners = new CopyOnWriteArraySet<>();

  private final Map<UUID, Long> slowedParticipants = new ConcurrentHashMap<>();

  private long slowDuration = DEFAULT_SLOW_DURATION;

  private final Predicate<Audience> joinCriteria;
  private final Predicate<Audience> talkCriteria;
  private final Predicate<Audience> slowTalkCriteria;
  private final Predicate<Audience> muteTalkCriteria;

  private final boolean shouldDefaultJoin;

  private boolean muted = false;
  private boolean slowed = false;

  public ChatChannel(ChatChannelBuilder builder) {
    this.identifier = builder.identifier;
    this.format = builder.format;

    this.joinCriteria = builder.joinCriteria;
    this.talkCriteria = builder.talkCriteria;
    this.slowTalkCriteria = builder.slowTalkCriteria;
    this.muteTalkCriteria = builder.muteTalkCriteria;

    this.shouldDefaultJoin = builder.shouldDefaultJoin;
    this.listeners.add(Bukkit.getConsoleSender());
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

  public boolean canJoin(Audience audience) {
    return joinCriteria.test(audience);
  }

  public boolean canTalk(Audience audience) {
    return talkCriteria.test(audience);
  }

  public boolean canSlowTalk(Audience audience) {
    return talkCriteria.and(slowTalkCriteria).test(audience);
  }

  public boolean canMuteTalk(Audience audience) {
    return talkCriteria.and(muteTalkCriteria).test(audience);
  }

  public ForwardingAudience getMembers() {
    Set<Audience> members = new CopyOnWriteArraySet<>(participants);
    members.addAll(listeners);
    return () -> members;
  }

  public void addParticipant(Audience audience) {
    participants.add(audience);
  }

  public void removeParticipant(Audience audience) {
    participants.remove(audience);
  }

  public void addListener(Audience audience) {
    listeners.add(audience);
  }

  public void removeListener(Audience audience) {
    listeners.remove(audience);
  }

  public boolean hasParticipant(Audience audience) {
    return participants.contains(audience);
  }

  public boolean hasListener(Audience audience) {
    return listeners.contains(audience);
  }

  public boolean hasMember(Audience audience) {
    return hasParticipant(audience) || hasListener(audience);
  }

  public boolean isMuted() {
    return muted;
  }

  public void setMuted(boolean muted) {
    this.muted = muted;
  }

  public long getSlowDuration() {
    return slowDuration;
  }

  public boolean isSlowed() {
    return slowed;
  }

  public void setUnSlowed() {
    this.slowed = false;
    slowedParticipants.clear();
  }

  public void setSlowed(long slowDuration) {
    this.slowed = true;
    this.slowDuration = slowDuration;
  }

  public void setSlowed() {
    setSlowed(DEFAULT_SLOW_DURATION);
  }

  public void slowParticipant(UUID uuid) {
    slowedParticipants.put(uuid, System.currentTimeMillis());
  }

  public void unSlowParticipant(UUID uuid) {
    slowedParticipants.remove(uuid);
  }

  public boolean isParticipantSlowed(UUID uuid) {
    Long startTime = slowedParticipants.get(uuid);
    if (startTime == null) {
      return false;
    }

    long elapsed = System.currentTimeMillis() - startTime;
    boolean result = elapsed < slowDuration;
    if (!result) {
      unSlowParticipant(uuid);
    }
    return result;
  }

  public Duration getRemaining(UUID uuid) {
    Long startTime = slowedParticipants.get(uuid);
    if (startTime == null) {
      return Duration.ZERO;
    }

    long remainingMillis = slowDuration - (System.currentTimeMillis() - startTime);
    return Duration.ofMillis(Math.max(remainingMillis, 0));
  }

  public boolean shouldDefaultJoin() {
    return shouldDefaultJoin;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChatChannel that = (ChatChannel) o;
    return Objects.equals(identifier, that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }

  public static final class ChatChannelBuilder {

    private final String identifier;
    private final String format;

    private Predicate<Audience> joinCriteria = audience -> true;
    private Predicate<Audience> talkCriteria = audience -> true;
    private Predicate<Audience> slowTalkCriteria = audience -> true;
    private Predicate<Audience> muteTalkCriteria = audience -> true;

    private boolean shouldDefaultJoin;

    public ChatChannelBuilder(String identifier, String format) {
      this.identifier = identifier.toLowerCase(Locale.ROOT);
      this.format = format;
    }

    public ChatChannelBuilder setJoinCriteria(Predicate<Audience> joinCriteria) {
      this.joinCriteria = Objects.requireNonNull(joinCriteria);
      return this;
    }

    public ChatChannelBuilder setTalkCriteria(Predicate<Audience> talkCriteria) {
      this.talkCriteria = Objects.requireNonNull(talkCriteria);
      return this;
    }

    public ChatChannelBuilder setSlowTalkCriteria(Predicate<Audience> slowTalkCriteria) {
      this.slowTalkCriteria = Objects.requireNonNull(slowTalkCriteria);
      return this;
    }

    public ChatChannelBuilder setMuteTalkCriteria(Predicate<Audience> muteTalkCriteria) {
      this.muteTalkCriteria = Objects.requireNonNull(muteTalkCriteria);
      return this;
    }

    public ChatChannelBuilder setShouldDefaultJoin(boolean shouldDefaultJoin) {
      this.shouldDefaultJoin = shouldDefaultJoin;
      return this;
    }

    public ChatChannel build() {
      return new ChatChannel(this);
    }
  }
}

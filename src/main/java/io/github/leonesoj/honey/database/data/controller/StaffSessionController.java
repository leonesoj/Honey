package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.data.model.StaffSession;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class StaffSessionController implements Listener {

  private final CacheStore cache;

  public StaffSessionController(CacheStore cache) {
    this.cache = cache;
  }

  public CompletableFuture<Optional<StaffSession>> getOrCreateSession(UUID uuid) {
    return cache.get(buildKey(uuid), StaffSession::deserializeFromJson)
        .thenCompose(optional -> {
          if (optional.isPresent()) {
            return CompletableFuture.completedFuture(optional);
          }

          StaffSession newSession = getDefaultSession(uuid);
          return cache.put(buildKey(uuid), newSession)
              .thenCompose(
                  staffSession -> completeOnMainThread(Optional.of(newSession)));
        });
  }

  public CompletableFuture<Boolean> modifySession(UUID uuid,
      Function<StaffSession, StaffSession> mutator) {
    return cache.get(buildKey(uuid), StaffSession::deserializeFromJson)
        .thenCompose(optional -> {
          StaffSession session = optional.orElseGet(() -> getDefaultSession(uuid));
          StaffSession mutated = mutator.apply(session);
          return cache.put(buildKey(uuid), mutated);
        });
  }

  private CompletableFuture<Optional<StaffSession>> completeOnMainThread(
      Optional<StaffSession> session) {
    CompletableFuture<Optional<StaffSession>> future = new CompletableFuture<>();
    Bukkit.getGlobalRegionScheduler().run(Honey.getInstance(), task -> future.complete(session));
    return future;
  }

  private StaffSession getDefaultSession(UUID uuid) {
    return new StaffSession(
        uuid,
        false,
        false,
        false,
        true
    );
  }

  private String buildKey(UUID uuid) {
    return uuid.toString() + ":staff_session";
  }

}

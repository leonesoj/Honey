package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.NoOpCache;
import io.github.leonesoj.honey.database.data.model.PlayerProfile;
import io.github.leonesoj.honey.database.providers.DataStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProfileController extends DataController<PlayerProfile> implements Listener {

  private final ConcurrentHashMap<UUID, Long> playTime = new ConcurrentHashMap<>();

  public ProfileController(DataStore data, boolean networkMode) {
    super(data,
        new NoOpCache(),
        new DataContainer(
            PlayerProfile.STORAGE_KEY,
            PlayerProfile.PRIMARY_INDEX,
            PlayerProfile.SCHEMA,
            Collections.emptySet()
        ),
        PlayerProfile::deserialize,
        null,
        Honey.getInstance(),
        false
    );

    if (!networkMode) {
      Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
    }
  }

  public CompletableFuture<Optional<PlayerProfile>> getPlayerProfile(UUID uuid) {
    return get(uuid);
  }

  @EventHandler
  public void onPreJoin(AsyncPlayerPreLoginEvent event) {
    UUID uuid = event.getUniqueId();

    final String serverId = Honey.getInstance().getServerId();

    try {
      Optional<PlayerProfile> optional = get(uuid).get();
      Instant now = Instant.now();

      if (optional.isPresent()) {
        PlayerProfile profile = optional.get();
        profile.setLastConnected(serverId);
        profile.setLastSeen(now);

        update(profile.getUuid(), profile);
      } else {
        PlayerProfile newProfile = new PlayerProfile(
            uuid,
            Honey.getInstance().getSecretHandler().hash(event.getAddress().getHostAddress()),
            now,
            now,
            Duration.ZERO,
            serverId
        );
        create(newProfile.getUuid(), newProfile);
      }
    } catch (InterruptedException | ExecutionException exception) {
      event.disallow(Result.KICK_OTHER, Component.translatable("honey.profile.failed"));
      getLogger().log(Level.SEVERE,
          "An error occurred while trying to process a PlayerProfile",
          exception
      );
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    playTime.put(event.getPlayer().getUniqueId(), System.nanoTime());
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    get(event.getPlayer().getUniqueId()).thenAccept(optional ->
        optional.ifPresent(profile -> {
          Instant now = Instant.now();

          profile.addPlayTime(Duration.ofMillis(endPlayTime(profile.getUuid())));
          profile.setLastSeen(now);
          update(profile.getUuid(), profile);
        })
    );
  }

  public Duration getCalculatedPlayTime(UUID uuid) {
    if (playTime.containsKey(uuid)) {
      return Duration.ofNanos(System.nanoTime() - playTime.getOrDefault(uuid, 0L));
    }
    return Duration.ZERO;
  }

  private long endPlayTime(UUID uuid) {
    long now = System.nanoTime();
    long nanos = now - playTime.remove(uuid);

    return Duration.ofNanos(nanos).toMillis();
  }
}

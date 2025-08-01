package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.NoOpCache;
import io.github.leonesoj.honey.database.data.model.PlayerProfile;
import io.github.leonesoj.honey.database.providers.DataStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProfileController extends DataController<PlayerProfile> implements Listener {

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

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    get(player.getUniqueId()).thenAccept(optional -> {
      Instant now = Instant.now();

      if (optional.isPresent()) {
        PlayerProfile profile = optional.get();
        profile.setLastConnected("hive1");
        profile.setLastSeen(now);

        update(profile.getUuid(), profile);
      } else {
        PlayerProfile newProfile = new PlayerProfile(
            player.getUniqueId(),
            now,
            now,
            Duration.ZERO,
            "hive1"
        );

        create(player.getUniqueId(), newProfile);
      }
    });
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    Player player = event.getPlayer();

    get(player.getUniqueId()).thenAccept(optional ->
        optional.ifPresent(profile -> {
          // TODO: Add playtime tracking
        })
    );
  }
}

package io.github.leonesoj.honey.features.staff.vanish;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.StaffSettingsController;
import io.github.leonesoj.honey.features.staff.vanish.providers.BukkitVanish;
import io.github.leonesoj.honey.features.staff.vanish.providers.VanishProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class VanishService implements Listener {

  private final Set<UUID> vanishedMembers = new HashSet<>();

  private final StaffSettingsController settingsController;

  private final VanishProvider vanishProvider;

  public VanishService(StaffSettingsController settingsController) {
    this.settingsController = settingsController;
    this.vanishProvider = new BukkitVanish(Honey.getInstance());

    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
  }

  public boolean toggleVanish(Player subject) {
    UUID subjectUuid = subject.getUniqueId();

    boolean isNowVanished = !vanishedMembers.remove(subjectUuid);
    if (isNowVanished) {
      vanishedMembers.add(subjectUuid);
    }

    setVanished(subject, isNowVanished);
    return isNowVanished;
  }

  public void setVanished(Player subject, boolean vanish) {
    UUID subjectUuid = subject.getUniqueId();

    if (vanish) {
      vanishedMembers.add(subjectUuid);
      subject.setMetadata("vanished", new FixedMetadataValue(Honey.getInstance(), true));
    } else {
      vanishedMembers.remove(subjectUuid);
      subject.removeMetadata("vanished", Honey.getInstance());
    }

    for (Player observer : Bukkit.getOnlinePlayers()) {
      if (observer.getUniqueId().equals(subjectUuid)) {
        continue;
      }

      if (!observer.hasPermission("honey.management.staff")) {
        if (vanish) {
          vanishProvider.hidePlayer(observer, subject);
        } else {
          vanishProvider.showPlayer(observer, subject);
        }
        continue;
      }

      final UUID observerUuid = observer.getUniqueId();
      settingsController.getSettingsSync(observerUuid)
          .thenAccept(optional -> optional.ifPresent(settings -> {
            boolean canSeeStaff = settings.inStaffMode() || settings.hasVisibleStaff();
            boolean hide = vanish && !canSeeStaff;
            if (hide) {
              vanishProvider.hidePlayer(observer, subject);
            } else {
              vanishProvider.showPlayer(observer, subject);
            }
          }));
    }
  }

  public void hideAllVanishedFor(Player observer) {
    UUID observerUuid = observer.getUniqueId();

    for (UUID uuid : vanishedMembers) {
      if (uuid.equals(observerUuid)) {
        continue;
      }
      Player subject = Bukkit.getPlayer(uuid);
      if (subject != null) {
        vanishProvider.hidePlayer(observer, subject);
      }
    }
  }

  public void showAllVanishedFor(Player observer) {
    UUID observerUuid = observer.getUniqueId();

    for (UUID uuid : vanishedMembers) {
      if (uuid.equals(observerUuid)) {
        continue;
      }
      Player subject = Bukkit.getPlayer(uuid);
      if (subject != null) {
        vanishProvider.showPlayer(observer, subject);
      }
    }
  }

  public boolean isVanished(UUID uuid) {
    return vanishedMembers.contains(uuid);
  }

  @EventHandler
  public void onServerListPing(PaperServerListPingEvent event) {
    event.setNumPlayers(
        Bukkit.getOnlinePlayers().size() - vanishedMembers.size()
    );
    event.getListedPlayers().removeIf(listedPlayer ->
        vanishedMembers.contains(listedPlayer.id())
    );
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (vanishedMembers.isEmpty()) {
      return;
    }

    Player observer = event.getPlayer();
    if (!observer.hasPermission("honey.management.staff")) {
      hideAllVanishedFor(observer);
      return;
    }

    settingsController.getSettingsSync(observer.getUniqueId())
        .thenAccept(optional -> {
          boolean canSeeStaff = optional
              .map(settings -> settings.inStaffMode() || settings.hasVisibleStaff())
              .orElse(false);

          if (canSeeStaff) {
            showAllVanishedFor(observer);
          } else {
            hideAllVanishedFor(observer);
          }
        });
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    vanishedMembers.remove(uuid);
  }

}

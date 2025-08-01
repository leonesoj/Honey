package io.github.leonesoj.honey.features.staff;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.StaffSessionController;
import io.github.leonesoj.honey.database.data.model.StaffState;
import io.github.leonesoj.honey.utils.other.DependCheck;
import io.github.leonesoj.honey.utils.vanish.BukkitVanish;
import io.github.leonesoj.honey.utils.vanish.ProtocolVanish;
import io.github.leonesoj.honey.utils.vanish.VanishProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class StaffHandler {

  private final StaffSessionController sessionController;

  private final Map<UUID, StaffState> staffMembers = new HashMap<>();

  private final Set<UUID> vanishedMembers = new HashSet<>();
  private final VanishProvider vanishProvider;

  private final StaffItems staffItems;

  public StaffHandler(StaffSessionController sessionController) {
    this.sessionController = sessionController;
    this.vanishProvider = DependCheck.isProtocolLibInstalled()
        ? new ProtocolVanish() : new BukkitVanish(Honey.getInstance());

    this.staffItems = new StaffItems(this);

    Bukkit.getPluginManager().registerEvents(new StaffListener(this), Honey.getInstance());
    Bukkit.getPluginManager()
        .registerEvents(new StaffModeListener(staffMembers), Honey.getInstance());
    Bukkit.getPluginManager().registerEvents(staffItems, Honey.getInstance());
  }

  public void toggleStaffMode(Player player) {
    if (staffMembers.containsKey(player.getUniqueId())) {
      restorePlayer(player);
      return;
    }

    setAsStaff(player);
  }

  public void setAsStaff(Player player) {
    staffMembers.put(player.getUniqueId(),
        new StaffState(player.getLocation(),
            player.getGameMode(),
            player.getInventory().getContents(),
            player.getActivePotionEffects(),
            player.getExp(),
            player.getLevel(),
            player.getWalkSpeed(),
            player.getFlySpeed()
        )
    );

    player.setGameMode(GameMode.CREATIVE);
    player.getInventory().clear();
    player.clearActivePotionEffects();
    player.setExp(0F);
    player.setLevel(0);
    player.setCanPickupItems(false);
    player.setSilent(true);
    player.setAffectsSpawning(false);
    player.setSleepingIgnored(true);
    player.setFireTicks(0);
    player.setAllowFlight(true);
    player.setWalkSpeed(0.2F);
    player.setFlySpeed(0.1F);

    staffItems.giveItems(player);

    sessionController.modifySession(player.getUniqueId(), session -> {
      session.setStaffMode(true);
      return session;
    });
  }

  public void restorePlayer(Player player) {
    player.setCanPickupItems(true);
    player.setSilent(false);

    StaffState state = staffMembers.get(player.getUniqueId());
    player.setGameMode(state.gameMode());
    player.getInventory().setContents(state.inventoryContents());
    player.addPotionEffects(state.effects());
    player.setExp(state.exp());
    player.setLevel(state.level());
    player.setWalkSpeed(state.walkSpeed());
    player.setFlySpeed(state.flySpeed());
    player.teleportAsync(state.location());

    staffMembers.remove(player.getUniqueId());
    sessionController.modifySession(player.getUniqueId(), session -> {
      session.setStaffMode(false);
      return session;
    });
  }

  public boolean toggleVanish(Player hiddenPlayer) {

    UUID hiddenUuid = hiddenPlayer.getUniqueId();
    boolean isNowVanished = !vanishedMembers.contains(hiddenUuid);

    if (isNowVanished) {
      vanishedMembers.add(hiddenUuid);
    } else {
      vanishedMembers.remove(hiddenUuid);
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      UUID uuid = player.getUniqueId();
      if (uuid.equals(hiddenUuid)) {
        continue;
      }

      if (player.hasPermission("honey.management.staff")) {
        sessionController.getOrCreateSession(uuid)
            .thenAccept(staffSession ->
                staffSession.ifPresent(session -> {
                  if (!vanishedMembers.contains(uuid) && session.isInStaffMode()
                      && !session.isStaffVisible()) {
                    if (isNowVanished) {
                      vanishProvider.hidePlayer(player, hiddenPlayer);
                    } else {
                      vanishProvider.showPlayer(player, hiddenPlayer);
                    }
                  }
                })
            );
      } else {
        if (isNowVanished) {
          vanishProvider.hidePlayer(player, hiddenPlayer);
        } else {
          vanishProvider.showPlayer(player, hiddenPlayer);
        }
      }
    }

    return isNowVanished;
  }

  public StaffSessionController getSessionController() {
    return sessionController;
  }

  public boolean isInStaffMode(UUID uuid) {
    return staffMembers.containsKey(uuid);
  }

}

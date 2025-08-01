package io.github.leonesoj.honey.features.staff;

import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import java.util.UUID;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class StaffListener implements Listener {

  private final StaffHandler staffHandler;

  public StaffListener(StaffHandler staffHandler) {
    this.staffHandler = staffHandler;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    if (player.hasPermission("honey.management.staff")) {
      event.joinMessage(null);

      staffHandler.getSessionController().getOrCreateSession(player.getUniqueId())
          .thenAccept(staffSession -> staffSession.ifPresent(session -> {
            if (session.isInStaffMode()) {
              staffHandler.setAsStaff(player);
            }
          }));
    }
  }

  @EventHandler
  public void onPlayerLogin(PlayerServerFullCheckEvent event) {
    if (!event.isAllowed()) {
      event.allow(
          hasPermissionOfflineWithVault(event.getPlayerProfile().getId(), "honey.management.staff")
      );
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();

    if (staffHandler.isInStaffMode(player.getUniqueId())) {
      staffHandler.restorePlayer(player);
    }
  }

  public boolean hasPermissionOfflineWithVault(UUID uuid, String permission) {
    RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager()
        .getRegistration(Permission.class);
    if (rsp != null) {
      Permission permissionProvider = rsp.getProvider();
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

      return permissionProvider.playerHas(null, offlinePlayer, permission);
    }
    return false;
  }

}

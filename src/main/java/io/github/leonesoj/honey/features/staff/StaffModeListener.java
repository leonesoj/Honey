package io.github.leonesoj.honey.features.staff;

import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class StaffModeListener implements Listener {

  private final StaffHandler staffHandler;

  public StaffModeListener(StaffHandler staffHandler) {
    this.staffHandler = staffHandler;
  }

  @EventHandler
  public void onItemPickUp(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    if (!staffHandler.isInStaffMode(player.getUniqueId())) {
      return;
    }
    event.setCancelled(staffHandler.getVanishService().isVanished(player.getUniqueId()));
  }

  @EventHandler
  public void onArrowPickup(PlayerPickupArrowEvent event) {
    if (staffHandler.isInStaffMode(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onAttack(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
      return;
    }
    if (!staffHandler.isInStaffMode(player.getUniqueId())) {
      return;
    }
    event.setCancelled(staffHandler.getVanishService().isVanished(player.getUniqueId()));
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    if (!staffHandler.isInStaffMode(uuid)) {
      return;
    }

    event.setCancelled(staffHandler.getVanishService().isVanished(uuid));
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    if (!staffHandler.isInStaffMode(uuid)) {
      return;
    }

    event.setCancelled(staffHandler.getVanishService().isVanished(uuid));
  }

  @EventHandler
  public void onGamemodeChange(PlayerGameModeChangeEvent event) {
    if (staffHandler.isInStaffMode(event.getPlayer().getUniqueId())
        && event.getNewGameMode() != GameMode.CREATIVE) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    if (staffHandler.isInStaffMode(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }
}

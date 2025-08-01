package io.github.leonesoj.honey.features.staff;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import io.github.leonesoj.honey.database.data.model.StaffState;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffModeListener implements Listener {

  private final Map<UUID, StaffState> staffModePlayers;

  public StaffModeListener(Map<UUID, StaffState> staffModePlayers) {
    this.staffModePlayers = staffModePlayers;
  }

  @EventHandler
  public void onItemDrop(PlayerDropItemEvent event) {
    if (staffModePlayers.containsKey(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    if (staffModePlayers.containsKey(event.getEntity().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent event) {
    if (staffModePlayers.containsKey(event.getPlayer().getUniqueId())) {
      event.quitMessage(null);
      staffModePlayers.remove(event.getPlayer().getUniqueId());
    }
  }

  @EventHandler
  public void onServerListPing(PaperServerListPingEvent event) {
    event.setNumPlayers(Bukkit.getOnlinePlayers().size() - staffModePlayers.size());
    event.getListedPlayers().removeIf(listedPlayer ->
        staffModePlayers.containsKey(listedPlayer.id())
    );
  }

}

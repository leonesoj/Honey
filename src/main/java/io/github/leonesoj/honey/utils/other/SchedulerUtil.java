package io.github.leonesoj.honey.utils.other;

import io.github.leonesoj.honey.Honey;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SchedulerUtil {

  private SchedulerUtil(){
  }

  public static void getPlayerScheduler(UUID playerUuid, Consumer<Player> consumer) {
    Bukkit.getGlobalRegionScheduler().run(Honey.getInstance(), scheduledTask -> {
      Player player = Bukkit.getPlayer(playerUuid);
      if (player != null && player.isOnline()) {
        player.getScheduler().run(Honey.getInstance(), task -> {
          consumer.accept(player);
        }, null);
      }
    });
  }

}

package io.github.leonesoj.honey.utils.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.github.leonesoj.honey.Honey;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class HeadUtils {

  private HeadUtils() {
  }

  public static void getPlayerHead(ItemBuilder item, String targetName, UUID uuid,
      Consumer<ItemBuilder> consumer) {
    Bukkit.getServer().getAsyncScheduler().runNow(Honey.getInstance(), asyncTask -> {
      OfflinePlayer offlinePlayer = (targetName != null)
          ? Bukkit.getOfflinePlayer(targetName)
          : Bukkit.getOfflinePlayer(uuid);

      PlayerProfile playerProfile = offlinePlayer.isOnline()
          ? offlinePlayer.getPlayerProfile() : Bukkit.createProfile(uuid, targetName);

      Bukkit.getGlobalRegionScheduler()
          .run(Honey.getInstance(),
              task -> consumer.accept(item.asPlayerHead(offlinePlayer, playerProfile))
          );
    });
  }

  public static void getPlayerHead(ItemBuilder item, String targetName,
      Consumer<ItemBuilder> consumer) {
    getPlayerHead(item, targetName, null, consumer);
  }

  public static void getPlayerHead(ItemBuilder item, UUID uuid, Consumer<ItemBuilder> consumer) {
    getPlayerHead(item, null, uuid, consumer);
  }

}

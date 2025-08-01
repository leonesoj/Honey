package io.github.leonesoj.honey.utils.other;

import io.github.leonesoj.honey.Honey;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class OfflinePlayerUtil {

  private OfflinePlayerUtil() {
  }

  public static void getAsyncOfflinePlayer(UUID uuid, Consumer<OfflinePlayer> consumer) {
    Bukkit.getAsyncScheduler().runNow(Honey.getInstance(), task -> {
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
      consumer.accept(offlinePlayer);
    });
  }

  public static void getAsyncOfflinePlayer(String username, Consumer<OfflinePlayer> consumer) {
    Bukkit.getAsyncScheduler().runNow(Honey.getInstance(), task -> {
      OfflinePlayer cachedPlayer = Bukkit.getOfflinePlayerIfCached(username);
      consumer.accept(
          Objects.requireNonNullElseGet(cachedPlayer, () -> Bukkit.getOfflinePlayer(username)));
    });
  }

}

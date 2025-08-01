package io.github.leonesoj.honey.utils.vanish;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitVanish extends VanishProvider {

  private final JavaPlugin plugin;

  public BukkitVanish(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void hidePlayer(Player observer, Player vanished) {
    observer.hidePlayer(plugin, vanished);
  }

  @Override
  public void showPlayer(Player observer, Player vanished) {
    observer.showPlayer(plugin, vanished);
  }
}

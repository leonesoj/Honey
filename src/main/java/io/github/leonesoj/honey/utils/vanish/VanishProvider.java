package io.github.leonesoj.honey.utils.vanish;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public abstract class VanishProvider {

  private final Set<UUID> vanishedPlayers = new HashSet<>();

  public abstract void hidePlayer(Player observer, Player vanished);

  public abstract void showPlayer(Player observer, Player vanished);

  protected Set<UUID> getVanishedPlayers() {
    return vanishedPlayers;
  }

}

package io.github.leonesoj.honey.features.staff.vanish.providers;

import org.bukkit.entity.Player;

public abstract class VanishProvider {

  public abstract void hidePlayer(Player observer, Player subject);

  public abstract void showPlayer(Player observer, Player subject);
}

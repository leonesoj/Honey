package io.github.leonesoj.honey.database.data.model;

import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public record StaffState(Location location, GameMode gameMode, ItemStack[] inventoryContents,
                         Collection<PotionEffect> effects,
                         float exp, int level, float walkSpeed, float flySpeed) {
}

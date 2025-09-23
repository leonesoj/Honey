package io.github.leonesoj.honey.database.data.model;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.utils.other.TinyFile;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public record StaffState(Location location, GameMode gameMode, ItemStack[] inventoryContents,
                         Collection<PotionEffect> effects,
                         float exp, int level, float walkSpeed, float flySpeed)
    implements ConfigurationSerializable {

  @Override
  public Map<String, Object> serialize() {
    return Map.of(
        "location", location.serialize(),
        "game_mode", gameMode.name(),
        "inventory_contents",
        Arrays.stream(inventoryContents == null ? new ItemStack[0] : inventoryContents)
            .map(item -> item == null ? null : item.serialize())
            .collect(Collectors.toList()),
        "potion_effects", effects.stream()
            .map(PotionEffect::serialize)
            .collect(Collectors.toList()),
        "exp", exp,
        "level", level,
        "walk_speed", walkSpeed,
        "fly_speed", flySpeed
    );
  }

  public static StaffState deserialize(Map<String, Object> map) {
    List<?> rawList = (List<?>) map.getOrDefault("inventory_contents", List.of());
    ItemStack[] inventoryContents = new ItemStack[rawList.size()];
    for (int i = 0; i < rawList.size(); i++) {
      Object o = rawList.get(i);
      if (o == null) {
        inventoryContents[i] = null;
      } else {
        inventoryContents[i] = ItemStack.deserialize((Map<String, Object>) o);
      }
    }

    List<Map<String, Object>> rawEffects = (List<Map<String, Object>>) map.getOrDefault(
        "potion_effects", List.of());
    Collection<PotionEffect> effects = rawEffects.stream()
        .map(PotionEffect::new)
        .toList();

    float exp = ((Number) map.getOrDefault("exp", 0.0)).floatValue();
    int level = ((Number) map.getOrDefault("level", 0)).intValue();
    float walk = ((Number) map.getOrDefault("walk_speed", 0.2F)).floatValue();
    float fly = ((Number) map.getOrDefault("fly_speed", 0.1F)).floatValue();

    return new StaffState(
        Location.deserialize((Map<String, Object>) map.get("location")),
        GameMode.valueOf(map.get("game_mode").toString()),
        inventoryContents,
        effects,
        exp,
        level,
        walk,
        fly
    );
  }

  public void saveStaffState(UUID uuid) {
    TinyFile file = new TinyFile(
        Honey.getInstance(),
        uuid.toString(),
        "staff_states"
    );
    if (!file.exists()) {
      file.editAndSave(config -> config.set("staff_state", this));
    }
  }

  public void clearStaffState(UUID uuid) {
    TinyFile file = new TinyFile(
        Honey.getInstance(),
        uuid.toString(),
        "staff_states"
    );
    file.deleteFile();
  }

  public static CompletableFuture<StaffState> loadStaffState(Player player) {
    CompletableFuture<StaffState> future = new CompletableFuture<>();

    TinyFile file = new TinyFile(
        Honey.getInstance(),
        player.getUniqueId().toString(),
        "staff_states"
    );
    file.loadConfig().thenAccept(config -> {
      StaffState state = config.getSerializable("staff_state", StaffState.class);
      player.getScheduler().run(Honey.getInstance(), task -> future.complete(state), null);
    });

    return future;
  }
}

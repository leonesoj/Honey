package io.github.leonesoj.honey.features.staff;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.utils.inventory.SerializedItem;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class StaffItems implements Listener {

  private final StaffHandler staffHandler;

  public StaffItems(StaffHandler staffHandler) {
    this.staffHandler = staffHandler;
  }

  public void giveItems(Player player) {
    Locale locale = player.locale();

    SerializedItem inspect = getConfigItem("inspect", locale);
    player.getInventory().setItem(inspect.slot(), inspect.item().build());

    SerializedItem randomTeleport = getConfigItem("random_teleport", locale);
    player.getInventory().setItem(randomTeleport.slot(), randomTeleport.item().build());

    SerializedItem followPlayer = getConfigItem("follow_player", locale);
    player.getInventory().setItem(followPlayer.slot(), followPlayer.item().build());

    SerializedItem vanish = getVanishItem(true, locale);
    player.getInventory().setItem(vanish.slot(), vanish.item().build());
  }

  private SerializedItem getConfigItem(String itemKey, Locale locale) {
    ConfigurationSection itemSection = Honey.getInstance().getConfigHandler().getStaffItems()
        .getConfigurationSection("items." + itemKey);
    return SerializedItem.parseItem(itemSection, locale);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (staffHandler.isInStaffMode(event.getPlayer().getUniqueId())
        && event.getAction().equals(Action.RIGHT_CLICK_AIR) && event.getItem() != null) {

      Player player = event.getPlayer();
      switch (event.getItem().getType()) {
        case EMERALD -> {
          Location randomPlayerLocation = getRandomTeleport(player);
          if (randomPlayerLocation != null) {
            player.teleportAsync(randomPlayerLocation);
          }
        }
        case LIME_DYE, GRAY_DYE -> {
          boolean result = staffHandler.toggleVanish(player);
          player.getInventory().setItemInMainHand(
              getVanishItem(result, player.locale()).item().build()
          );
        }
        default -> {
          // no-op
        }
      }

    }
  }

  @EventHandler
  public void onItemDrag(InventoryDragEvent event) {
    event.getWhoClicked()
        .sendMessage(Component.text(event.getOldCursor().getType().name(), NamedTextColor.RED));
    if (staffHandler.isInStaffMode(event.getWhoClicked().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  private SerializedItem getVanishItem(boolean status, Locale locale) {
    SerializedItem item = getConfigItem("vanish", locale);

    if (!status) {
      item.item()
          .setMaterial(Material.matchMaterial(item.otherData().get("disabled_type").toString()));
    }

    item.item().setDisplayName(item.item().build()
        .displayName()
        .appendSpace()
        .append(MiniMessage.miniMessage().deserialize(
            item.otherData().get(status ? "enabled_text" : "disabled_text").toString())
        )
    );

    return item;
  }

  private Location getRandomTeleport(Player self) {
    List<Player> candidates = Bukkit.getOnlinePlayers().stream()
        .filter(player ->
            !player.equals(self) && !staffHandler.isInStaffMode(player.getUniqueId())
        )
        .collect(Collectors.toList());

    if (candidates.isEmpty()) {
      return null;
    }

    Player target = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    return target.getLocation();
  }

}

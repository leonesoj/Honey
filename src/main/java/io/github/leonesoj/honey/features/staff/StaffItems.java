package io.github.leonesoj.honey.features.staff;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.utils.inventory.SerializedItem;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class StaffItems implements Listener {

  private final StaffHandler staffHandler;

  private final NamespacedKey staffItemKey;

  public StaffItems(StaffHandler staffHandler) {
    this.staffHandler = staffHandler;
    this.staffItemKey = NamespacedKey.fromString("staff_item", Honey.getInstance());
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
    ConfigurationSection itemSection = Honey.getInstance().getTranslationHandler()
        .findBestTranslation("staff-items", locale)
        .getConfigurationSection("items." + itemKey);
    SerializedItem item = SerializedItem.parseItem(itemSection);
    item.item().build().editPersistentDataContainer(
        pdc -> pdc.set(staffItemKey, PersistentDataType.BOOLEAN, true)
    );
    return item;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!staffHandler.isInStaffMode(player.getUniqueId())) {
      return;
    }
    if (event.getItem() == null || event.getAction() != Action.RIGHT_CLICK_AIR) {
      return;
    }

    Map<String, Material> itemMap = getInteractItems();
    Material vanishEnabled = itemMap.get("vanish_enabled");
    Material vanishDisabled = itemMap.get("vanish_disabled");
    Material randomTeleport = itemMap.get("random_teleport");

    Material itemMaterial = event.getItem().getType();

    if (itemMaterial.equals(vanishDisabled) || itemMaterial.equals(vanishEnabled)) {
      boolean result = staffHandler.getVanishService().toggleVanish(player);
      player.getInventory().setItemInMainHand(
          getVanishItem(result, player.locale()).item().build()
      );
    } else if (itemMaterial.equals(randomTeleport)) {
      Location randomPlayerLocation = getRandomTeleport(player);
      if (randomPlayerLocation != null) {
        player.teleportAsync(randomPlayerLocation);
      }
    }
  }

  @EventHandler
  public void onPlayerClick(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    if (!staffHandler.isInStaffMode(player.getUniqueId())) {
      return;
    }
    if (!(event.getRightClicked() instanceof Player clickedPlayer)) {
      return;
    }

    Map<String, Material> itemMap = getPlayerInteractItems();
    Material followMaterial = itemMap.get("follow_player");
    Material inspectMaterial = itemMap.get("inspect");

    Material itemMaterial = player.getInventory().getItem(event.getHand()).getType();

    if (itemMaterial.equals(inspectMaterial)) {
      if (!staffHandler.isInStaffMode(clickedPlayer.getUniqueId())) {
        player.openInventory(clickedPlayer.getInventory());
      }
    } else if (itemMaterial.equals(followMaterial)) {
      clickedPlayer.addPassenger(player);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }
    if (!staffHandler.isInStaffMode(player.getUniqueId())) {
      return;
    }

    ItemStack clickedItem = event.getCurrentItem();
    if (clickedItem == null) {
      return;
    }
    ItemStack hotbarItem =
        event.getHotbarButton() != -1
            ? player.getInventory().getItem(event.getHotbarButton()) : null;

    if (isStaffItem(clickedItem) || isStaffItem(hotbarItem)) {
      event.setCancelled(true);
      player.updateInventory();
    }
  }

  @EventHandler
  public void onSwapHand(PlayerSwapHandItemsEvent event) {
    if (staffHandler.isInStaffMode(event.getPlayer().getUniqueId())) {
      event.setCancelled(isStaffItem(event.getOffHandItem()));
    }
  }

  @EventHandler
  public void onItemDrag(InventoryDragEvent event) {
    if (staffHandler.isInStaffMode(event.getWhoClicked().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    if (staffHandler.isInStaffMode(event.getPlayer().getUniqueId())) {
      event.setCancelled(isStaffItem(event.getItemDrop().getItemStack()));
    }
  }

  private SerializedItem getVanishItem(boolean status, Locale locale) {
    SerializedItem item = getConfigItem("vanish", locale);

    if (!status) {
      item.item()
          .setMaterial(Material.matchMaterial(item.otherData().get("disabled_type").toString()));
    }

    item.item().addPlaceHolder("status",
        MiniMessage.miniMessage().deserialize(
            item.otherData().get(status ? "enabled_text" : "disabled_text").toString())
    );

    return item;
  }

  private Map<String, Material> getInteractItems() {
    return Map.of(
        "vanish_enabled", getVanishItem(true, Locale.ENGLISH).item().build().getType(),
        "vanish_disabled", getVanishItem(false, Locale.ENGLISH).item().build().getType(),
        "random_teleport", getConfigItem("random_teleport", Locale.ENGLISH).item().build().getType()
    );
  }

  private Map<String, Material> getPlayerInteractItems() {
    return Map.of(
        "inspect", getConfigItem("inspect", Locale.ENGLISH).item().build().getType(),
        "follow_player", getConfigItem("follow_player", Locale.ENGLISH).item().build().getType()
    );
  }

  private boolean isStaffItem(ItemStack itemStack) {
    if (itemStack == null) {
      return false;
    }

    return itemStack.getPersistentDataContainer().has(staffItemKey);
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

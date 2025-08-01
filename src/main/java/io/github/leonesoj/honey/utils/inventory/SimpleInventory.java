package io.github.leonesoj.honey.utils.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SimpleInventory implements Listener {

  private final JavaPlugin plugin;

  private final Inventory inventory;
  private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();

  private final SimpleInventory parent;
  private final List<SimpleInventory> children = new ArrayList<>();

  private boolean registered;

  public SimpleInventory(JavaPlugin plugin, Component title, int size, SimpleInventory parent) {
    this.plugin = plugin;
    this.inventory = Bukkit.createInventory(null, size, title);
    this.parent = parent;
  }

  protected abstract void build();

  public void open(Player player) {
    if (parent != null && !parent.getChildren().contains(this)) {
      throw new IllegalStateException(
          "Parent did not register this SimpleInventory instance as a child"
      );
    }

    build();

    if (!registered) {
      Bukkit.getPluginManager().registerEvents(this, plugin);
      registered = true;
    }

    player.openInventory(inventory);
  }

  protected void addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> event) {
    if (slot == -1) {
      return;
    }

    inventory.setItem(slot, item);
    if (event != null) {
      clickHandlers.put(slot, event);
    }
  }

  protected void addItem(int slot, ItemStack item) {
    addItem(slot, item, null);
  }

  protected void packItem(ItemStack item, Consumer<InventoryClickEvent> event) {
    addItem(inventory.firstEmpty(), item, event);
  }

  protected void registerChild(SimpleInventory child) {
    children.add(child);
  }

  protected void openChild(Player player, int index) {
    if (index < 0 || index >= children.size()) {
      throw new IllegalArgumentException("Invalid child index: " + index);
    }
    children.get(index).open(player);
  }

  protected void goBack(Player player) {
    Objects.requireNonNull(parent, "No parent assigned to go back");

    int index = parent.children.indexOf(this);
    if (index == 0) {
      parent.open(player);
    } else {
      parent.children.get(index - 1).open(player);
    }
  }

  protected void goForward(Player player) {
    Objects.requireNonNull(parent, "No parent assigned to go forward");

    int index = parent.children.indexOf(this) + 1;
    if (index >= parent.children.size()) {
      throw new IllegalStateException("Already at the last child.");
    }

    parent.children.get(index).open(player);
  }

  protected void goToParent(Player player) {
    Objects.requireNonNull(parent, "No parent assigned to go to parent");
    parent.open(player);
  }

  protected void goToRoot(Player player) {
    Objects.requireNonNull(parent, "No parent assigned to go to root");

    SimpleInventory grandParent = parent;
    while (grandParent.parent != null) {
      grandParent = grandParent.parent;
    }

    grandParent.open(player);
  }

  protected Inventory getInventory() {
    return inventory;
  }

  protected SimpleInventory getParent() {
    return parent;
  }

  protected List<SimpleInventory> getChildren() {
    return children;
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!event.getInventory().equals(inventory)) {
      return;
    }

    event.setCancelled(true);
    int clickedSlot = event.getRawSlot();
    if (clickHandlers.containsKey(clickedSlot)) {
      clickHandlers.get(clickedSlot).accept(event);
    }
  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getInventory().equals(inventory)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!event.getInventory().equals(inventory)) {
      return;
    }

    if (event.getReason() != Reason.OPEN_NEW && registered) {
      HandlerList.unregisterAll(this);
      registered = false;

      if (parent != null && parent.registered) {
        HandlerList.unregisterAll(parent);

        parent.children.forEach(child -> {
          if (child.registered) {
            unregisterChildren(child);
          }
        });

        SimpleInventory grandParent = parent.parent;
        while (grandParent != null) {
          HandlerList.unregisterAll(grandParent);
          grandParent = grandParent.parent;
        }
      }

      children.forEach(child -> {
        if (child.registered) {
          unregisterChildren(child);
        }
      });

      onFinalClose();
    }
  }

  public JavaPlugin getPlugin() {
    return plugin;
  }

  protected void onFinalClose() {
  }

  private void unregisterChildren(SimpleInventory inventory) {
    HandlerList.unregisterAll(inventory);
    for (SimpleInventory child : inventory.children) {
      unregisterChildren(child);
    }
  }

}

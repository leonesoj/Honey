package io.github.leonesoj.honey.utils.inventory;

import io.github.leonesoj.honey.utils.item.ItemBuilder;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryDecorator {

  private InventoryDecorator() {
  }

  public static void addFiller(Inventory inventory, ItemStack fillerItem, boolean force) {
    for (int i = 0; i < inventory.getSize(); ++i) {
      if (inventory.getItem(i) == null || force) {
        inventory.setItem(i, fillerItem);
      }
    }
  }

  public static void addFiller(Inventory inventory, ItemStack fillerItem) {
    addFiller(inventory, fillerItem, false);
  }

  public static void addCheckerboard(Inventory inventory, ItemStack fillerOne,
      ItemStack fillerTwo, boolean force) {
    for (int i = 0; i < inventory.getSize(); i++) {
      if (inventory.getItem(i) == null || force) {
        int row = i / 9;
        int col = i % 9;

        if ((row + col) % 2 == 0) {
          inventory.setItem(i, fillerOne);
        } else {
          inventory.setItem(i, fillerTwo);
        }
      }
    }
  }

  public enum BorderPattern {
    SOLID, CHECKERED
  }

  public static void addBorder(Inventory inventory, ItemStack borderItem) {
    addBorder(inventory, borderItem, null, BorderPattern.SOLID, false);
  }

  public static void addBorder(Inventory inventory,
      ItemStack primary,
      ItemStack secondary,
      BorderPattern pattern,
      boolean force) {
    int size = inventory.getSize();
    if (size % 9 != 0) {
      throw new IllegalStateException("Inventory size must be a multiple of 9");
    }

    int rows = size / 9;
    if (rows < 3) {
      throw new IllegalStateException("Inventory contains less than 3 rows");
    }

    for (int i = 0; i < size; i++) {
      if (!isBorderSlot(i, rows)) {
        continue;
      }
      if (inventory.getItem(i) != null && !force) {
        continue;
      }

      if (pattern == BorderPattern.SOLID || secondary == null) {
        inventory.setItem(i, primary);
      } else {
        int row = i / 9;
        int col = i % 9;
        inventory.setItem(i, ((row + col) % 2 == 0) ? primary : secondary);
      }
    }
  }

  private static boolean isBorderSlot(int index, int rows) {
    int row = index / 9;
    int col = index % 9;
    return row == 0 || row == rows - 1 || col == 0 || col == 8;
  }

  public static void createLoadingScreen(Inventory inventory) {
    ItemStack fillerItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1)
        .setDisplayName(Component.text(""))
        .build();

    ItemStack loadingItem = new ItemBuilder(Material.BOOKSHELF, 1)
        .setDisplayName(Component.text("Loading...", NamedTextColor.GOLD))
        .build();

    addFiller(inventory, fillerItem);
    inventory.setItem(getMiddleSlot(inventory.getSize()), loadingItem);
  }

  public static void createErrorScreen(SimpleInventory simpleInventory,
      Consumer<InventoryClickEvent> event) {

    ItemStack fillerItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE, 1)
        .setDisplayName(Component.text(""))
        .build();

    ItemStack errorItem = new ItemBuilder(Material.BARRIER, 1)
        .setDisplayName(Component.text("An error occurred", NamedTextColor.RED))
        .build();

    Inventory inventory = simpleInventory.getInventory();

    addFiller(inventory, fillerItem);
    simpleInventory.addItem(getMiddleSlot(inventory.getSize()), errorItem, event);
  }

  private static int getMiddleSlot(int inventorySize) {
    int rows = inventorySize / 9;
    return ((rows / 2) * 9) + 4;
  }

}

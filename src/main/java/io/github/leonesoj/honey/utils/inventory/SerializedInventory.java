package io.github.leonesoj.honey.utils.inventory;

import io.github.leonesoj.honey.utils.inventory.InventoryDecorator.BorderPattern;
import io.github.leonesoj.honey.utils.item.ItemBuilder;
import java.util.Locale;
import java.util.function.Consumer;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SerializedInventory extends SimpleInventory {

  private final ConfigurationSection mainSection;
  private final ConfigurationSection decoratorSection;
  private final ConfigurationSection itemsSection;

  private final Locale locale;

  public SerializedInventory(JavaPlugin plugin, ConfigurationSection section, Locale locale,
      SimpleInventory parent) {
    super(plugin,
        section.getRichMessage("title", Component.empty()),
        section.getInt("size", 54),
        parent
    );
    this.mainSection = section;
    this.decoratorSection = section.getConfigurationSection("decorator");
    this.itemsSection = section.getConfigurationSection("items");
    this.locale = locale;
  }

  public SerializedInventory(JavaPlugin plugin, FileConfiguration config, Locale locale,
      SimpleInventory parent) {
    this(plugin, config.getConfigurationSection("main"), locale, parent);
  }

  protected abstract void buildContent();

  @Override
  protected final void build() {
    applyDecorator(false);
    buildContent();
  }

  protected void applyDecorator(boolean force) {
    String decorator = decoratorSection.getString("type", "none")
        .toLowerCase(Locale.ROOT);

    switch (decorator) {
      case "border" -> InventoryDecorator.addBorder(getInventory(),
          new ItemBuilder(decoratorSection.getConfigurationSection("border_item")).build()
      );
      case "checkered_border" -> InventoryDecorator.addBorder(getInventory(),
          new ItemBuilder(decoratorSection.getConfigurationSection("border_item_one")).build(),
          new ItemBuilder(decoratorSection.getConfigurationSection("border_item_two")).build(),
          BorderPattern.CHECKERED,
          force
      );
      case "checkerboard" -> InventoryDecorator.addCheckerboard(getInventory(),
          new ItemBuilder(
              decoratorSection.getConfigurationSection("checkerboard_item_one")).build(),
          new ItemBuilder(
              decoratorSection.getConfigurationSection("checkerboard_item_two")).build(),
          force
      );
      case "fill" -> InventoryDecorator.addFiller(getInventory(),
          new ItemBuilder(decoratorSection.getConfigurationSection("fill_item")).build(),
          force
      );
      case "none" -> {
      }
      default -> getPlugin().getLogger().warning(
          "Invalid decorator type: %s".formatted(decorator)
      );
    }
  }

  protected SerializedItem parseItem(ConfigurationSection section) {
    return SerializedItem.parseItem(section);
  }

  protected SerializedItem parseItem(String path) {
    return SerializedItem.parseItem(itemsSection.getConfigurationSection(path));
  }

  protected void addItem(SerializedItem item, Consumer<InventoryClickEvent> consumer) {
    if (item.slot() >= getInventory().getSize() || item.slot() < -1) {
      return;
    }

    if (item.slot() == -1) {
      packItem(item.item().build(), event -> clickEvent(item, event, consumer));
    } else {
      addItem(item.slot(), item.item().build(),
          event -> clickEvent(item, event, consumer));
    }
  }

  protected void addItem(SerializedItem item) {
    addItem(item, null);
  }

  protected void packItem(SerializedItem item, Consumer<InventoryClickEvent> consumer) {
    packItem(item.item().build(), event -> clickEvent(item, event, consumer));
  }

  private void clickEvent(SerializedItem item, InventoryClickEvent event,
      Consumer<InventoryClickEvent> consumer) {
    if (item.clickSound() != null) {
      event.getWhoClicked().playSound(
          Sound.sound(item.clickSound(), Source.MASTER, 1.0F, 0.1F)
      );
    }
    if (consumer != null) {
      consumer.accept(event);
    }
  }

  public final ConfigurationSection getRootSection() {
    return mainSection.getRoot();
  }

  public final ConfigurationSection getItemSection(String path) {
    return mainSection.getConfigurationSection(path);
  }

  protected final Locale getLocale() {
    return locale;
  }
}

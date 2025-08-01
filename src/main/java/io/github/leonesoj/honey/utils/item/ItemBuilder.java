package io.github.leonesoj.honey.utils.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.github.leonesoj.honey.utils.other.LocaleUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public class ItemBuilder {

  private ItemStack item;

  public ItemBuilder(Material material, int amount) {
    this.item = ItemStack.of(material, amount);
  }

  public ItemBuilder(ItemStack item) {
    this.item = item;
  }

  public ItemBuilder(@Nullable ConfigurationSection section, Locale locale) {
    if (section == null) {
      this.item = new ItemStack(Material.STONE, 1);
      setDisplayName("Missing item data");
      addLore(Component.text("Check that your config file is setup correctly"));
      return;
    }

    this.item = ItemStack.of(
        Objects.requireNonNullElse(Material.matchMaterial(section.getString("type", "STONE")),
            Material.STONE),
        section.getInt("amount", 1)
    );

    setDisplayName(LocaleUtil.translate(section, "display_name", locale));
    setLore(LocaleUtil.translateList(section.getConfigurationSection("lore"), locale));

    setGlint(section.getBoolean("glint", false));
    showAttributes(section.getBoolean("show_attributes", false));

    setCustomModelData(section.getSerializable("custom_model_data",
        CustomModelDataComponent.class)
    );
  }

  private Component stripDefaultItalic(Component component) {
    if (!component.hasDecoration(TextDecoration.ITALIC)) {
      return component.decoration(TextDecoration.ITALIC, false);
    }
    return component;
  }

  public ItemBuilder setDisplayName(Component displayName) {
    item.editMeta(itemMeta -> itemMeta.customName(stripDefaultItalic(displayName)));
    return this;
  }

  public ItemBuilder setDisplayName(String displayName) {
    return setDisplayName(MiniMessage.miniMessage().deserialize(displayName));
  }

  public ItemBuilder addPlaceHolder(Consumer<TextReplacementConfig.Builder> consumer) {
    item.editMeta(itemMeta -> {
      if (itemMeta.hasCustomName()) {
        itemMeta.customName(itemMeta.customName().replaceText(consumer));
      }
      if (itemMeta.hasLore()) {
        List<Component> loreLines = item.lore().stream().map(component ->
            component.replaceText(consumer)).toList();
        itemMeta.lore(loreLines);
      }
    });
    return this;
  }

  public ItemBuilder addPlaceHolder(String literal, Component component) {
    addPlaceHolder(builder ->
        builder.matchLiteral("{%s}".formatted(literal)).replacement(component)
    );
    return this;
  }

  public ItemBuilder addPlaceHolder(String literal, String replacement) {
    addPlaceHolder(literal, Component.text(replacement));
    return this;
  }

  public ItemBuilder addPlaceholder(Map<String, Component> placeholders) {
    placeholders.forEach((literal, component) ->
        addPlaceHolder(builder -> builder.matchLiteral(literal).replacement(component))
    );
    return this;
  }

  public ItemBuilder addLore(Component loreLine) {
    item.editMeta(itemMeta -> {
      List<Component> lore = itemMeta.hasLore() ? itemMeta.lore() : new ArrayList<>();
      lore.add(loreLine);
      itemMeta.lore(lore);
    });
    return this;
  }

  public ItemBuilder setLore(List<Component> loreLines) {
    List<Component> strippedLines = loreLines.stream()
        .map(this::stripDefaultItalic)
        .toList();
    item.editMeta(itemMeta -> itemMeta.lore(strippedLines));
    return this;
  }

  public ItemBuilder setMaterial(Material material) {
    item = item.withType(material);
    return this;
  }

  public ItemBuilder setGlint(boolean glint) {
    item.editMeta(itemMeta -> itemMeta.setEnchantmentGlintOverride(glint));
    return this;
  }

  public ItemBuilder setCustomModelData(CustomModelDataComponent customModelData) {
    item.editMeta(itemMeta -> itemMeta.setCustomModelDataComponent(customModelData));
    return this;
  }

  public ItemBuilder showAttributes(boolean showAttributes) {
    if (!showAttributes) {
      item.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }
    return this;
  }

  public ItemBuilder setData(CustomModelData customModelData) {
    item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
    return this;
  }

  public ItemBuilder asPlayerHead(OfflinePlayer offlinePlayer, @Nullable PlayerProfile profile) {
    if (item.getType() != Material.PLAYER_HEAD) {
      return this;
    }

    item.editMeta(SkullMeta.class, meta -> {
      meta.setOwningPlayer(offlinePlayer);
      meta.setPlayerProfile(profile);
    });
    return this;
  }

  public ItemStack build() {
    return item;
  }

}
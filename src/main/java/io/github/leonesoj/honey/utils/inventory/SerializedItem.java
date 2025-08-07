package io.github.leonesoj.honey.utils.inventory;

import io.github.leonesoj.honey.utils.item.ItemBuilder;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public record SerializedItem(int slot, ItemBuilder item, Sound clickSound,
                             Map<String, Object> otherData) {

  public static SerializedItem parseItem(ConfigurationSection section, Locale locale) {
    ConfigurationSection otherDataSection = section.getConfigurationSection("other_data");
    Map<String, Object> otherData;
    if (otherDataSection == null) {
      otherData = Collections.emptyMap();
    } else {
      otherData = otherDataSection.getValues(true);
    }

    org.bukkit.Sound sound = null;
    if (section.getString("sound") != null) {
      sound = Registry.SOUND_EVENT.get(
          NamespacedKey.fromString(section.getString("sound"))
      );
    }

    return new SerializedItem(section.getInt("slot", -1),
        new ItemBuilder(section, locale),
        sound,
        otherData
    );
  }

}

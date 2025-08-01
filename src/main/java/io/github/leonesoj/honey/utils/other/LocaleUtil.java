package io.github.leonesoj.honey.utils.other;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

public class LocaleUtil {

  private LocaleUtil() {
  }

  public static Component translate(ConfigurationSection rootSection, String path, Locale locale) {
    if (rootSection.isConfigurationSection(path)) {
      ConfigurationSection section = rootSection.getConfigurationSection(path);
      if (section != null) {
        return section.getRichMessage(matchLocale(section, locale), Component.empty());
      }
    }

    return rootSection.getRichMessage(path, Component.empty());
  }

  public static List<Component> translateList(ConfigurationSection section,
      Locale locale) {
    if (section == null) {
      return Collections.emptyList();
    }

    return section.getStringList(matchLocale(section, locale)).stream()
        .map(line -> MiniMessage.miniMessage().deserialize(line))
        .toList();
  }

  private static String matchLocale(ConfigurationSection section, Locale locale) {
    Set<Locale> availableLocales = section.getKeys(false).stream()
        .map(Locale::forLanguageTag)
        .collect(Collectors.toSet());

    List<LanguageRange> preferences = Locale.LanguageRange.parse(locale.toLanguageTag());
    Locale match = Locale.lookup(preferences, availableLocales);
    if (match == null) {
      match = Locale.US;
    }

    return match.toLanguageTag();
  }

}

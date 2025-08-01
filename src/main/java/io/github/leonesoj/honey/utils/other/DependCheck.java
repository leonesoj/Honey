package io.github.leonesoj.honey.utils.other;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DependCheck {

  private DependCheck() {
  }

  public static boolean isVaultInstalled() {
    return isPluginInstalled("Vault");
  }

  public static boolean isLuckPermsInstalled() {
    return isPluginInstalled("LuckPerms");
  }

  public static boolean isProtocolLibInstalled() {
    return isPluginInstalled("ProtocolLib");
  }

  public static boolean isPlaceholderApiInstalled() {
    return isPluginInstalled("PlaceholderAPI");
  }

  public static boolean isApolloInstalled() {
    return isPluginInstalled("Apollo-Bukkit");
  }

  private static boolean isPluginInstalled(String pluginName) {
    Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
    if (plugin == null) {
      return false;
    }

    return plugin.isEnabled();
  }

}

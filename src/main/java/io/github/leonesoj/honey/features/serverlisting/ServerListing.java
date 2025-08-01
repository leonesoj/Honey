package io.github.leonesoj.honey.features.serverlisting;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import io.github.leonesoj.honey.Honey;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.CachedServerIcon;

public class ServerListing implements Listener {

  private static final Map<String, CachedServerIcon> serverIcons = new HashMap<>();

  private static final String LISTING_PATH = "server.listing";
  private static final String BEE_ICON_FILE_PATH = "icons/bee_icon.png";
  private static final String HONEY_ICON_FILE_PATH = "icons/honey_icon.png";

  public ServerListing() {
    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());

    if (!fileExists(BEE_ICON_FILE_PATH)) {
      Honey.getInstance().saveResource(BEE_ICON_FILE_PATH, false);
    }
    if (!fileExists(HONEY_ICON_FILE_PATH)) {
      Honey.getInstance().saveResource(HONEY_ICON_FILE_PATH, false);
    }

    File iconsDir = new File(Honey.getInstance().getDataFolder(), "icons");
    File[] files = iconsDir.listFiles((dir, name) -> name.endsWith(".png"));
    if (files != null) {
      for (File file : files) {
        try {
          CachedServerIcon icon = Bukkit.loadServerIcon(file);
          serverIcons.put(file.getName().split("\\.")[0], icon);
        } catch (Exception exception) {
          Honey.getInstance().getLogger().log(Level.WARNING,
              "Failed to server icon: " + file.getName(),
              exception
          );
        }
      }
    } else {
      Honey.getInstance().getLogger()
          .warning("Failed to load server icons: icons directory does not exist.");
    }
  }

  @EventHandler
  public void onServerListPing(PaperServerListPingEvent event) {
    ConfigurationSection config = Honey.getInstance().getConfig()
        .getConfigurationSection(LISTING_PATH);

    event.setHidePlayers(config.getBoolean("hide_player_count", false));
    if (!config.getBoolean("show_player_list", true)) {
      event.getListedPlayers().clear();
    }

    ConfigurationSection motdSection = config.getConfigurationSection("motd");
    ConfigurationSection randomMotd = motdSection.getConfigurationSection(
        getRandomElement(motdSection.getKeys(false))
    );

    String serverIcon = randomMotd.getString("icon", "random");
    if (serverIcon.equalsIgnoreCase("random") && !serverIcons.isEmpty()) {
      event.setServerIcon(serverIcons.get(getRandomElement(serverIcons.keySet())));
    } else if (serverIcons.containsKey(serverIcon)) {
      event.setServerIcon(serverIcons.get(serverIcon));
    }

    event.motd(randomMotd.getStringList("lines").stream()
        .map(s -> MiniMessage.miniMessage().deserialize(s))
        .reduce(Component::append).orElse(Component.empty())
    );
  }


  private String getRandomElement(Set<String> set) {
    if (set.isEmpty()) {
      return null;
    }

    int index = ThreadLocalRandom.current().nextInt(set.size());
    return set.stream().skip(index).findFirst().orElse(null);
  }

  private boolean fileExists(String path) {
    return new File(Honey.getInstance().getDataFolder(), path).exists();
  }


}

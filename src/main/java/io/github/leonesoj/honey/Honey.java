package io.github.leonesoj.honey;

import io.github.leonesoj.honey.chat.ChatChannel;
import io.github.leonesoj.honey.chat.ChatChannel.ChatChannelBuilder;
import io.github.leonesoj.honey.chat.ChatService;
import io.github.leonesoj.honey.config.Config;
import io.github.leonesoj.honey.database.DataHandler;
import io.github.leonesoj.honey.features.serverlisting.ServerListing;
import io.github.leonesoj.honey.features.staff.StaffHandler;
import io.github.leonesoj.honey.locale.TranslationHandler;
import io.github.leonesoj.honey.secret.SecretHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class Honey extends JavaPlugin {

  private static Honey instance;

  public static Honey getInstance() {
    return instance;
  }

  private Config config;

  private DataHandler dataHandler;

  private StaffHandler staffHandler;

  private ChatService chatService;

  private TranslationHandler translationHandler;

  private SecretHandler secretHandler;

  @Override
  public void onEnable() {
    instance = this;

    config = new Config(this, "", "config", false);

    dataHandler = new DataHandler(this,
        getConfig().getString("database.provider"),
        getConfig().getConfigurationSection("database.config"),
        getConfig().getString("cache.provider"),
        getConfig().getConfigurationSection("cache.redis_config"),
        getServerId()
    );

    secretHandler = new SecretHandler(this);
    secretHandler.generateSecret();

    staffHandler = new StaffHandler(dataHandler.getStaffSettingsController());

    chatService = new ChatService();
    registerDefaultChannels();

    translationHandler = new TranslationHandler(this);
    translationHandler.loadTranslationStore();
    translationHandler.registerTranslationConfigs();

    new ServerListing();

    registerServerLinks();
  }

  @Override
  public void onDisable() {
    instance = null;

    if (dataHandler != null) {
      dataHandler.closeConnection();
    }
  }

  public Config config() {
    return config;
  }

  public CompletableFuture<Void> reloadConfigAsync() {
    return config.loadConfig();
  }

  public String getServerId() {
    return getConfig().getString("server.identifier", "hive1");
  }

  public boolean isNetworked() {
    return getConfig().getBoolean("network.enabled", false);
  }

  public DataHandler getDataHandler() {
    return dataHandler;
  }

  public StaffHandler getStaffHandler() {
    return staffHandler;
  }

  public ChatService getChatService() {
    return chatService;
  }

  public TranslationHandler getTranslationHandler() {
    return translationHandler;
  }

  public SecretHandler getSecretHandler() {
    return secretHandler;
  }

  public void registerServerLinks() {
    config().getRawConfig().getConfigurationSection("server")
        .getConfigurationSection("links").getValues(false).forEach((entry, value) -> {
          ConfigurationSection section = (ConfigurationSection) value;
          try {
            Bukkit.getServerLinks().addLink(
                section.getRichMessage("display_name"),
                new URI(section.getString("url"))
            );
          } catch (URISyntaxException e) {
            getLogger().warning("Found invalid URL for server link: " + entry);
          }
        });
  }

  private void registerDefaultChannels() {
    ChatChannel generalChat = new ChatChannelBuilder("general",
        config().getString("chat.channels.general.format"))
        .setShouldDefaultJoin(true)
        .setSlowDuration(config().getInt("chat.slow_duration") * 1000L)
        .setMuteTalkCriteria(audience -> {
          Optional<UUID> optional = audience.get(Identity.UUID);
          return optional.map(
                  uuid -> Bukkit.getPlayer(uuid).hasPermission("honey.chat.general.bypass.mute"))
              .orElse(true);
        })
        .setSlowTalkCriteria(audience -> {
          Optional<UUID> optional = audience.get(Identity.UUID);
          return optional.map(
                  uuid -> Bukkit.getPlayer(uuid).hasPermission("honey.chat.general.bypass.slow"))
              .orElse(true);
        })
        .build();
    chatService.registerChannel(generalChat);
    chatService.setDefaultChannel(generalChat);

    chatService.registerChannel(
        new ChatChannelBuilder("staff", config().getString("chat.channels.staff.format"))
            .setJoinCriteria(audience -> {
              Optional<UUID> optional = audience.get(Identity.UUID);
              return optional.map(
                      uuid -> Bukkit.getPlayer(uuid).hasPermission("honey.moderation.staff"))
                  .orElse(true);
            })
            .build()
    );
  }

}

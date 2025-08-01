package io.github.leonesoj.honey.database;

import io.github.leonesoj.honey.database.cache.CacheProvider;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.cache.InMemoryCache;
import io.github.leonesoj.honey.database.cache.RedisCache;
import io.github.leonesoj.honey.database.data.controller.ReportController;
import io.github.leonesoj.honey.database.data.controller.SettingsController;
import io.github.leonesoj.honey.database.data.controller.StaffSessionController;
import io.github.leonesoj.honey.database.providers.DataProvider;
import io.github.leonesoj.honey.database.providers.DataStore;
import io.github.leonesoj.honey.database.providers.MongoData;
import io.github.leonesoj.honey.database.providers.MySqlData;
import io.github.leonesoj.honey.database.providers.SqliteData;
import io.lettuce.core.RedisConnectionException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class DataHandler {

  private final JavaPlugin plugin;
  private final ConfigurationSection databaseConfig;

  private final DataStore dataStore;
  private final CacheStore cache;

  private final SettingsController settingsController;
  private final StaffSessionController staffSessionController;
  private final ReportController reportController;

  public DataHandler(JavaPlugin plugin, String databaseProvider,
      ConfigurationSection databaseConfig, String cacheProvider, ConfigurationSection redisConfig) {
    this.plugin = plugin;
    this.databaseConfig = databaseConfig;

    this.dataStore = initHoneyData(databaseProvider);
    this.cache = initCacheProvider(cacheProvider, redisConfig);

    this.settingsController = new SettingsController(dataStore, cache);
    this.staffSessionController = new StaffSessionController(cache);
    this.reportController = new ReportController(dataStore);
  }

  public SettingsController getSettingsController() {
    return settingsController;
  }

  public ReportController getReportController() {
    return reportController;
  }

  public StaffSessionController getStaffSessionController() {
    return staffSessionController;
  }

  private DataStore initHoneyData(String databaseProvider) {
    return switch (databaseProvider.toLowerCase()) {
      case "mongodb" ->
          new MongoData(databaseConfig.getString("connection_string"), plugin.getLogger());
      case "mysql" -> new MySqlData(databaseConfig.getString("host"),
          databaseConfig.getInt("port"),
          databaseConfig.getString("database"),
          databaseConfig.getString("user"),
          databaseConfig.getString("password"), plugin.getLogger());
      default -> new SqliteData(plugin.getDataPath(), databaseConfig.getString("database"),
          plugin.getLogger());
    };
  }

  private CacheStore initCacheProvider(String cacheProvider, ConfigurationSection redisConfig) {
    switch (cacheProvider.toLowerCase()) {
      case "redis":
        try {
          return new RedisCache(
              redisConfig.getString("host"),
              redisConfig.getInt("port"),
              redisConfig.getString("password"),
              plugin.getLogger()
          );
        } catch (RedisConnectionException e) {
          plugin.getLogger().warning("Falling back to In-Memory cache");
          return new InMemoryCache(plugin.getLogger());
        }
      case "memory", "in-memory":
        return new InMemoryCache(plugin.getLogger());
      default:
        plugin.getLogger().warning(
            "Cache provider '%s' is not supported. Falling back to In-Memory cache"
                .formatted(cacheProvider)
        );
        return new InMemoryCache(plugin.getLogger());
    }
  }

  public CacheProvider getCacheProvider() {
    return cache.getProvider();
  }

  public DataProvider getDataProvider() {
    return dataStore.getProvider();
  }

  public void closeConnection() {
    dataStore.closeConnection();
    cache.shutdownCache();
  }

}

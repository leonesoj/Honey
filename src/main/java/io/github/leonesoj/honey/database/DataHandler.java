package io.github.leonesoj.honey.database;

import io.github.leonesoj.honey.database.cache.CacheProvider;
import io.github.leonesoj.honey.database.cache.CacheStore;
import io.github.leonesoj.honey.database.cache.InMemoryCache;
import io.github.leonesoj.honey.database.cache.NoOpCache;
import io.github.leonesoj.honey.database.cache.RedisCache;
import io.github.leonesoj.honey.database.data.controller.ProfileController;
import io.github.leonesoj.honey.database.data.controller.ReportController;
import io.github.leonesoj.honey.database.data.controller.SettingsController;
import io.github.leonesoj.honey.database.data.controller.StaffSettingsController;
import io.github.leonesoj.honey.database.providers.DataProvider;
import io.github.leonesoj.honey.database.providers.DataStore;
import io.github.leonesoj.honey.database.providers.MySqlData;
import io.github.leonesoj.honey.database.providers.SqliteData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class DataHandler {

  private final JavaPlugin plugin;
  private final ConfigurationSection databaseConfig;

  private final DataStore dataStore;

  private final CacheStore nearCache;
  private final CacheStore sharedCache;

  private final SettingsController settingsController;
  private final ProfileController profileController;
  private final StaffSettingsController staffSettingsController;
  private final ReportController reportController;

  public DataHandler(JavaPlugin plugin,
      String databaseProvider,
      ConfigurationSection databaseConfig,
      String cacheProvider,
      ConfigurationSection redisConfig,
      String serverId) {
    this.plugin = plugin;
    this.databaseConfig = databaseConfig;

    this.dataStore = initDataStore(parseDataProvider(databaseProvider));

    CacheInit cacheInit = initCaches(parseCacheProvider(cacheProvider), redisConfig, serverId);
    this.nearCache = cacheInit.near();
    this.sharedCache = cacheInit.shared();

    this.settingsController = new SettingsController(dataStore, nearCache, sharedCache);
    this.staffSettingsController = new StaffSettingsController(dataStore, nearCache, sharedCache);

    this.profileController = new ProfileController(dataStore, false);
    this.reportController = new ReportController(dataStore);
  }

  public SettingsController getSettingsController() {
    return settingsController;
  }

  public ProfileController getProfileController() {
    return profileController;
  }

  public ReportController getReportController() {
    return reportController;
  }

  public StaffSettingsController getStaffSettingsController() {
    return staffSettingsController;
  }

  private DataStore initDataStore(DataProvider provider) {
    return switch (provider) {
      case MYSQL -> new MySqlData(
          databaseConfig.getString("host"),
          databaseConfig.getInt("port"),
          databaseConfig.getString("database"),
          databaseConfig.getString("username"),
          databaseConfig.getString("password"),
          plugin.getLogger()
      );
      case SQLITE -> new SqliteData(
          plugin.getDataPath(),
          databaseConfig.getString("database"),
          plugin.getLogger()
      );
    };
  }

  private record CacheInit(CacheStore near, CacheStore shared) {

  }

  private CacheInit initCaches(CacheProvider provider, ConfigurationSection redis,
      String serverId) {
    CacheStore near = new InMemoryCache(plugin.getLogger());
    switch (provider) {
      case REDIS -> {
        try {
          CacheStore shared = new RedisCache(
              redis.getString("host"),
              redis.getInt("port"),
              redis.getString("password"),
              redis.getBoolean("ssl"),
              serverId,
              plugin.getLogger()
          );
          return new CacheInit(near, shared);
        } catch (RuntimeException e) {
          plugin.getLogger()
              .warning("Redis unavailable: " + e.getMessage());
          return new CacheInit(near, new NoOpCache());
        }
      }
      case IN_MEMORY, NO_OP -> {
        return new CacheInit(near, new NoOpCache());
      }
    }
    return new CacheInit(near, new NoOpCache());
  }

  public CacheProvider getNearCacheProvider() {
    return nearCache.getProvider();
  }

  public CacheProvider getSharedCacheProvider() {
    return sharedCache.getProvider();
  }

  public DataProvider getDataProvider() {
    return dataStore.getProvider();
  }

  public void closeConnection() {
    dataStore.closeConnection();
    nearCache.shutdownCache();
    sharedCache.shutdownCache();
  }

  private static DataProvider parseDataProvider(String provider) {
    String string = normalize(provider);
    return switch (string) {
      case "mysql" -> DataProvider.MYSQL;
      case "sqlite", "" -> DataProvider.SQLITE;
      default -> DataProvider.SQLITE;
    };
  }

  private static CacheProvider parseCacheProvider(String provider) {
    String string = normalize(provider);
    return switch (string) {
      case "redis" -> CacheProvider.REDIS;
      case "in-memory", "memory" -> CacheProvider.IN_MEMORY;
      case "noop", "no_op", "no-op", "none", "" -> CacheProvider.NO_OP;
      default -> CacheProvider.NO_OP;
    };
  }

  private static String normalize(String s) {
    return (s == null) ? "" : s.trim().toLowerCase();
  }

}

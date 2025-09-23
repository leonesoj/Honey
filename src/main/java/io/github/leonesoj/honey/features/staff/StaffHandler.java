package io.github.leonesoj.honey.features.staff;

import static io.github.leonesoj.honey.locale.Message.argComponent;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.SpyService;
import io.github.leonesoj.honey.database.data.controller.StaffSettingsController;
import io.github.leonesoj.honey.database.data.model.StaffSettings;
import io.github.leonesoj.honey.database.data.model.StaffState;
import io.github.leonesoj.honey.features.staff.vanish.VanishService;
import io.github.leonesoj.honey.utils.other.DependCheck;
import io.github.leonesoj.honey.utils.other.SchedulerUtil;
import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class StaffHandler implements Listener {

  private final Set<UUID> staffMembers = new HashSet<>();

  private final StaffSettingsController settingsController;

  private final VanishService vanishService;
  private final SpyService spyService;

  private final StaffItems staffItems;

  public StaffHandler(StaffSettingsController settingsController) {
    this.settingsController = settingsController;
    this.vanishService = new VanishService(settingsController);
    this.staffItems = new StaffItems(this);
    this.spyService = new SpyService();

    ConfigurationSerialization.registerClass(StaffState.class);

    Bukkit.getPluginManager().registerEvents(this, Honey.getInstance());
    Bukkit.getPluginManager().registerEvents(new StaffModeListener(this), Honey.getInstance());
    Bukkit.getPluginManager().registerEvents(staffItems, Honey.getInstance());

    if (DependCheck.isLuckPermsInstalled()) {
      new StaffPermsListener(this).startListening();
    }
  }

  public void toggleStaffMode(Player player) {
    if (staffMembers.contains(player.getUniqueId())) {
      restorePlayer(player);
      return;
    }
    setAsStaff(player);
  }

  public void setAsStaff(Player player) {
    staffMembers.add(player.getUniqueId());
    vanishService.setVanished(player, true);
    vanishService.showAllVanishedFor(player);

    StaffState staffState = new StaffState(
        player.getLocation(),
        player.getGameMode(),
        player.getInventory().getContents(),
        player.getActivePotionEffects(),
        player.getExp(),
        player.getLevel(),
        player.getWalkSpeed(),
        player.getFlySpeed()
    );
    staffState.saveStaffState(player.getUniqueId());

    player.setGameMode(GameMode.CREATIVE);
    player.getInventory().clear();
    player.clearActivePotionEffects();
    player.setExp(0F);
    player.setLevel(0);
    player.setWalkSpeed(0.2F);
    player.setFlySpeed(0.1F);
    player.setAllowFlight(true);
    player.setFireTicks(0);
    player.setSilent(true);
    player.setAffectsSpawning(false);
    player.setSleepingIgnored(true);
    player.setCollidable(false);

    settingsController.modifySettings(player.getUniqueId(), staffSettings -> {
      staffSettings.setStaffMode(true);
      return staffSettings;
    });

    staffItems.giveItems(player);
  }

  public void restorePlayer(Player player) {
    player.setSilent(false);
    player.setAffectsSpawning(true);
    player.setSleepingIgnored(false);
    player.setCollidable(true);

    StaffState.loadStaffState(player)
        .thenAccept(state -> {
          player.setGameMode(state.gameMode());
          player.getInventory().setContents(state.inventoryContents());
          player.addPotionEffects(state.effects());
          player.setExp(state.exp());
          player.setLevel(state.level());
          player.setWalkSpeed(state.walkSpeed());
          player.setFlySpeed(state.flySpeed());
          player.teleportAsync(state.location());
          state.clearStaffState(player.getUniqueId());
        });

    vanishService.setVanished(player, false);

    settingsController.modifySettings(player.getUniqueId(), staffSettings -> {
      staffSettings.setStaffMode(false);

      SchedulerUtil.getPlayerScheduler(player.getUniqueId(), p -> {
        if (!staffSettings.hasVisibleStaff()) {
          vanishService.hideAllVanishedFor(player);
        }
      });

      return staffSettings;
    });

    staffMembers.remove(player.getUniqueId());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    if (player.hasPermission("honey.management.staff")) {
      settingsController.getSettingsSync(player.getUniqueId())
          .thenAccept(optional -> optional.ifPresent(settings -> {
            if (settings.inStaffMode() && settings.shouldPersistStaffMode()) {
              setAsStaff(player);
            } else if (settings.inStaffMode() && !settings.shouldPersistStaffMode()) {
              restorePlayer(player);
            }
            if (!settings.inStaffMode() && settings.hasVisibleStaff()) {
              vanishService.showAllVanishedFor(player);
            } else if (!settings.hasVisibleStaff() && !settings.inStaffMode()) {
              vanishService.hideAllVanishedFor(player);
            }
            if (settings.hasSocialSpy()) {
              spyService.toggleGlobalSpy(player.getUniqueId());
            }
          }));

      event.joinMessage(null);
      broadcastToStaff(
          Component.translatable("honey.staff.join",
              argComponent("player", player.getName()),
              argComponent("server", Honey.getInstance().getServerId())
          )
      );
    } else {
      vanishService.hideAllVanishedFor(player);
    }
  }

  @EventHandler
  public void onFullServerJoin(PlayerServerFullCheckEvent event) {
    if (!event.isAllowed()) {
      event.allow(
          hasPermissionOfflineWithVault(event.getPlayerProfile().getId())
      );
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (player.hasPermission("honey.management.staff")) {
      if (staffMembers.contains(player.getUniqueId())) {
        staffMembers.remove(player.getUniqueId());
        event.quitMessage(null);
      }
      broadcastToStaff(
          Component.translatable("honey.staff.quit",
              argComponent("player", player.getName()),
              argComponent("server", Honey.getInstance().getServerId())
          )
      );
    }
  }

  private boolean hasPermissionOfflineWithVault(UUID uuid) {
    RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager()
        .getRegistration(Permission.class);
    if (rsp != null) {
      Permission permissionProvider = rsp.getProvider();
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
      return permissionProvider.playerHas(null, offlinePlayer, "honey.management.staff");
    }
    return false;
  }

  private void broadcastToStaff(Component component) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!player.hasPermission("honey.management.staff")) {
        continue;
      }

      settingsController.getSettingsSync(player.getUniqueId())
          .thenAccept(optional -> {
            boolean receivesAlerts = optional
                .map(StaffSettings::hasStaffAlerts)
                .orElse(true);

            if (receivesAlerts) {
              player.sendMessage(component);
            }
          });
    }
  }

  public boolean isInStaffMode(UUID uuid) {
    return staffMembers.contains(uuid);
  }

  public VanishService getVanishService() {
    return vanishService;
  }

  public SpyService getSpyService() {
    return spyService;
  }
}
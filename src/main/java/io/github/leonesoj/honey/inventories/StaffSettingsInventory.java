package io.github.leonesoj.honey.inventories;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.StaffSettingsController;
import io.github.leonesoj.honey.database.data.model.StaffSettings;
import io.github.leonesoj.honey.features.staff.StaffHandler;
import io.github.leonesoj.honey.utils.inventory.InventoryDecorator;
import io.github.leonesoj.honey.utils.inventory.SerializedInventory;
import io.github.leonesoj.honey.utils.inventory.SerializedItem;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StaffSettingsInventory extends SerializedInventory {

  private final UUID uuid;
  private final StaffSettingsController controller = Honey.getInstance()
      .getDataHandler()
      .getStaffSettingsController();

  public StaffSettingsInventory(UUID uuid, Locale locale) {
    super(Honey.getInstance(),
        Honey.getInstance().getConfigHandler().getSettingsGui()
            .getConfigurationSection("staff_settings"),
        locale,
        null
    );
    this.uuid = uuid;
  }

  @Override
  protected void buildContent() {
    InventoryDecorator.createLoadingScreen(getInventory());

    controller.getSettingsSync(uuid)
        .exceptionally(throwable -> {
          InventoryDecorator.createErrorScreen(this, null);
          return Optional.empty();
        })
        .thenAccept(optional -> {
          if (optional.isPresent()) {
            StaffSettings settings = optional.get();
            applyDecorator(true);

            addItem(prepareItem(SettingType.SHOW_STAFF, settings.hasVisibleStaff()),
                toggle(settings, SettingType.SHOW_STAFF));
            addItem(prepareItem(SettingType.SOCIAL_SPY, settings.hasSocialSpy()),
                toggle(settings, SettingType.SOCIAL_SPY));
            addItem(prepareItem(SettingType.PERSIST_STAFF_MODE, settings.shouldPersistStaffMode()),
                toggle(settings, SettingType.PERSIST_STAFF_MODE));
            addItem(prepareItem(SettingType.REPORT_ALERTS, settings.hasReportAlerts()),
                toggle(settings, SettingType.REPORT_ALERTS));
            addItem(prepareItem(SettingType.STAFF_ALERTS, settings.hasStaffAlerts()),
                toggle(settings, SettingType.STAFF_ALERTS));
          }
        });
  }

  private Consumer<InventoryClickEvent> toggle(StaffSettings settings, SettingType type) {
    return event -> {
      Player player = (Player) event.getWhoClicked();
      StaffHandler staffHandler = Honey.getInstance().getStaffHandler();

      switch (type) {
        case SHOW_STAFF -> {
          settings.toggleVisibleStaff();

          if (!settings.inStaffMode()) {
            if (settings.hasVisibleStaff()) {
              staffHandler.getVanishService().showAllVanishedFor(player);
            } else {
              staffHandler.getVanishService().hideAllVanishedFor(player);
            }
          }

        }
        case SOCIAL_SPY -> {
          boolean newStatus = !settings.hasSocialSpy();
          staffHandler.getSpyService().setSpyStatus(uuid, newStatus);
          settings.setSocialSpy(!settings.hasSocialSpy());
        }
        case PERSIST_STAFF_MODE -> settings.togglePersistStaffMode();
        case REPORT_ALERTS -> settings.toggleReportAlerts();
        case STAFF_ALERTS -> settings.toggleStaffAlerts();
      }

      controller.updateSettingsSync(settings)
          .exceptionally(throwable -> {
            player.closeInventory();
            return false;
          })
          .thenAccept(success -> {
            if (success) {
              boolean status = false;
              switch (type) {
                case SHOW_STAFF -> status = settings.hasVisibleStaff();
                case SOCIAL_SPY -> status = settings.hasSocialSpy();
                case PERSIST_STAFF_MODE -> status = settings.shouldPersistStaffMode();
                case REPORT_ALERTS -> status = settings.hasReportAlerts();
                case STAFF_ALERTS -> status = settings.hasStaffAlerts();
              }
              event.setCurrentItem(prepareItem(type, status).item().build());
            }
          });
    };
  }

  private SerializedItem prepareItem(SettingType type, boolean status) {
    SerializedItem serializedItem = parseItem(type.name().toLowerCase());
    serializedItem.item().addPlaceHolder(statusPlaceholder(status));
    return serializedItem;
  }

  private Consumer<Builder> statusPlaceholder(boolean status) {
    return builder ->
        builder.matchLiteral("%status%")
            .replacement(status ? Component.text("ENABLED ✔", NamedTextColor.GREEN) :
                Component.text("DISABLED ✖", NamedTextColor.RED)
            );
  }

  private enum SettingType {
    SHOW_STAFF,
    SOCIAL_SPY,
    PERSIST_STAFF_MODE,
    REPORT_ALERTS,
    STAFF_ALERTS
  }


}

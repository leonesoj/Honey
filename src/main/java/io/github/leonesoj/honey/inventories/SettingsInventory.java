package io.github.leonesoj.honey.inventories;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.SettingsController;
import io.github.leonesoj.honey.database.data.model.PlayerSettings;
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

public class SettingsInventory extends SerializedInventory {

  private final UUID uuid;
  private final SettingsController controller = Honey.getInstance().getDataHandler()
      .getSettingsController();

  public SettingsInventory(UUID uuid, Locale locale) {
    super(Honey.getInstance(),
        Honey.getInstance().getConfigHandler().getSettingsGui(),
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
            PlayerSettings settings = optional.get();
            applyDecorator(true);

            addItem(prepareItem(SettingType.CHAT_MESSAGES, settings.hasChatMessages()),
                toggle(settings, SettingType.CHAT_MESSAGES));
            addItem(prepareItem(SettingType.PRIVATE_MESSAGES, settings.hasPrivateMessages()),
                toggle(settings, SettingType.PRIVATE_MESSAGES));
            addItem(prepareItem(SettingType.PROFANITY_FILTER, settings.hasProfanityFilter()),
                toggle(settings, SettingType.PROFANITY_FILTER));
            addItem(prepareItem(SettingType.SOUND_ALERTS, settings.hasSoundAlerts()),
                toggle(settings, SettingType.SOUND_ALERTS));
          }
        });
  }

  private Consumer<InventoryClickEvent> toggle(PlayerSettings settings, SettingType type) {
    return event -> {
      Player player = (Player) event.getWhoClicked();

      switch (type) {
        case CHAT_MESSAGES -> settings.toggleChatMessages();
        case PRIVATE_MESSAGES -> settings.togglePrivateMessages();
        case PROFANITY_FILTER -> settings.toggleProfanityFilter();
        case SOUND_ALERTS -> settings.toggleSoundAlerts();
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
                case CHAT_MESSAGES -> status = settings.hasChatMessages();
                case PRIVATE_MESSAGES -> status = settings.hasPrivateMessages();
                case PROFANITY_FILTER -> status = settings.hasProfanityFilter();
                case SOUND_ALERTS -> status = settings.hasSoundAlerts();
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
    CHAT_MESSAGES,
    PRIVATE_MESSAGES,
    PROFANITY_FILTER,
    SOUND_ALERTS;
  }

}

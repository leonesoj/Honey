package io.github.leonesoj.honey.features.staff;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.StaffSettingsController;
import io.github.leonesoj.honey.utils.other.SchedulerUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;

public class StaffPermsListener {

  private final StaffHandler handler;

  public StaffPermsListener(StaffHandler handler) {
    this.handler = handler;
  }

  public void startListening() {
    EventBus eventBus = LuckPermsProvider.get().getEventBus();

    final StaffSettingsController controller = Honey.getInstance().getDataHandler()
        .getStaffSettingsController();

    eventBus.subscribe(Honey.getInstance(), NodeRemoveEvent.class, event -> {
      if (event.getNode().getKey().equals("honey.management.staff")
          && event.getTarget() instanceof User user) {
        SchedulerUtil.getPlayerScheduler(user.getUniqueId(), player -> {
          if (handler.isInStaffMode(player.getUniqueId())) {
            handler.restorePlayer(player);
          }
        });
        controller.deleteSettings(user.getUniqueId());
      }
    });

    eventBus.subscribe(Honey.getInstance(), NodeAddEvent.class, event -> {
      if (event.getNode().getKey().equals("honey.management.staff")
          && event.getTarget() instanceof User user) {
        controller.createSettings(user.getUniqueId());
      }
    });
  }

}

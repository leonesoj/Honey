package io.github.leonesoj.honey.inventories;

import static io.github.leonesoj.honey.locale.Message.argComponent;
import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.model.Report;
import io.github.leonesoj.honey.database.data.model.Report.ReportStatus;
import io.github.leonesoj.honey.locale.Message;
import io.github.leonesoj.honey.utils.inventory.ReactiveInventory;
import io.github.leonesoj.honey.utils.inventory.SerializedItem;
import io.github.leonesoj.honey.utils.inventory.SimpleInventory;
import io.github.leonesoj.honey.utils.other.SchedulerUtil;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ReportViewInventory extends ReactiveInventory<Report> {

  private final Report report;

  public ReportViewInventory(Report report, Locale locale, SimpleInventory parent) {
    super(Honey.getInstance(),
        Honey.getInstance().getConfigHandler().getReportViewerGui()
            .getConfigurationSection("report_view"),
        locale,
        Honey.getInstance().getDataHandler().getReportController().getObserverService(),
        parent
    );

    this.report = report;
  }

  @Override
  protected void buildContent() {
    getInventory().clear();

    SerializedItem issuer = parseItem("issuer");
    addItem(issuer);

    SerializedItem subject = parseItem("subject");
    addItem(subject);

    ZonedDateTime zonedDateTime = report.getTimestamp().atZone(ZoneId.systemDefault());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    SerializedItem reportItem = parseItem("report_item");
    reportItem.item()
        .addPlaceHolder("short-id", Component.text(report.getId().toString().substring(0, 8)))
        .addPlaceHolder("long-id", Component.text(report.getId().toString()))
        .addPlaceHolder("reason", Component.text(report.getReason()))
        .addPlaceHolder("status", Component.text(report.getStatus().name()))
        .addPlaceHolder("date", Component.text(zonedDateTime.format(formatter)))
        .addPlaceHolder("server", Component.text(report.getServer()));
    addItem(reportItem);

    if (report.getStatus().equals(ReportStatus.PENDING_REVIEW)) {
      addItem(parseItem("mark_as_resolved"), changeReportStatus(ReportStatus.RESOLVED));
      addItem(parseItem("mark_as_noted"), changeReportStatus(ReportStatus.NOTED));
    }

    if (getParent() != null) {
      SerializedItem backItem = parseItem("back_item");
      addItem(backItem, event -> goToParent((Player) event.getWhoClicked()));
    }

    addItem(parseItem("exit_item"), event -> event.getWhoClicked().closeInventory());

    addItem(parseItem("delete_item"), deleteReport());

    Bukkit.getAsyncScheduler().runNow(Honey.getInstance(), task -> {
      OfflinePlayer issuerPlayer = Bukkit.getOfflinePlayer(report.getIssuer());
      PlayerProfile issuerProfile = getProfile(issuerPlayer);

      OfflinePlayer subjectPlayer = Bukkit.getOfflinePlayer(report.getSubject());
      PlayerProfile subjectProfile = getProfile(subjectPlayer);

      subject.item()
          .asPlayerHead(subjectPlayer, subjectProfile)
          .addPlaceHolder("name", subjectPlayer.getName())
          .addPlaceHolder("status", fancyStatus(subjectPlayer.isOnline()));
      addItem(subject);

      issuer.item()
          .asPlayerHead(issuerPlayer, issuerProfile)
          .addPlaceHolder("name", issuerPlayer.getName())
          .addPlaceHolder("status", fancyStatus(issuerPlayer.isOnline()));
      addItem(issuer);
    });

  }

  private Consumer<InventoryClickEvent> changeReportStatus(ReportStatus status) {
    return event -> {
      report.setStatus(status);
      Honey.getInstance().getDataHandler().getReportController().updateReport(report);

      int resolvedSlot = parseItem("mark_as_resolved").slot();
      int notedSlot = parseItem("mark_as_noted").slot();
      addItem(resolvedSlot, null);
      addItem(notedSlot, null);
      applyDecorator(false);
    };
  }

  private Consumer<InventoryClickEvent> deleteReport() {
    return event -> {
      Player player = (Player) event.getWhoClicked();
      player.closeInventory();

      Honey.getInstance().getDataHandler().getReportController().deleteReport(report.getId())
          .thenAccept(result -> SchedulerUtil.getPlayerScheduler(player.getUniqueId(), p -> {
            if (result) {
              p.sendMessage(prefixed("honey.report.deleted.success",
                  argComponent("id", report.getId().toString().substring(0, 8)))
              );
            } else {
              p.sendMessage(prefixed("honey.report.deleted.failure"));
            }
          }));
    };
  }

  private Component fancyStatus(boolean online) {
    return online ? Component.text("online").color(NamedTextColor.GREEN)
        : Component.text("offline").color(NamedTextColor.RED);
  }

  private PlayerProfile getProfile(OfflinePlayer player) {
    return player.isOnline() ? player.getPlayerProfile()
        : Bukkit.createProfile(player.getUniqueId(), player.getName());
  }

  @Override
  public boolean matches(Report other) {
    return report.getId().equals(other.getId());
  }

  @Override
  public boolean matchesId(UUID uuid) {
    return report.getId().equals(uuid);
  }

  @Override
  public void onUpdate(Report other) {
    report.setStatus(other.getStatus());
    reOpenForViewers();
  }

  @Override
  public void onDelete(UUID other) {
    List<HumanEntity> snapshot = new ArrayList<>(getInventory().getViewers());
    for (HumanEntity viewer : snapshot) {
      viewer.sendMessage(Message.prefixed("honey.report.deleted.react"));
      viewer.closeInventory();
    }
  }
}


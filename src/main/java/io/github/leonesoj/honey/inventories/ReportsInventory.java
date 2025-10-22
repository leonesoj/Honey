package io.github.leonesoj.honey.inventories;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.model.Report;
import io.github.leonesoj.honey.utils.inventory.InventoryDecorator;
import io.github.leonesoj.honey.utils.inventory.ReactiveInventory;
import io.github.leonesoj.honey.utils.inventory.SerializedItem;
import io.github.leonesoj.honey.utils.inventory.SimpleInventory;
import io.github.leonesoj.honey.utils.other.DurationUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ReportsInventory extends ReactiveInventory<Report> {

  private static final int REPORTS_PER_INVENTORY = 28;

  private int offset;

  public ReportsInventory(Locale locale, int offset) {
    super(Honey.getInstance(),
        Honey.getInstance().getConfigHandler().getReportViewerGui(),
        locale,
        Honey.getInstance().getDataHandler().getReportController().getObserverService(),
        null
    );
    this.offset = offset;
  }

  @Override
  protected void buildContent() {
    InventoryDecorator.createLoadingScreen(getInventory());

    Honey.getInstance().getDataHandler().getReportController()
        .getPendingReports(REPORTS_PER_INVENTORY, offset)
        .thenAccept(reports -> {
          getInventory().clear(); // janky
          applyDecorator(true);

          addItem(parseItem("exit_item"),
              event -> event.getWhoClicked().closeInventory());

          reports.forEach(report -> {
            UUID id = report.getId();
            OfflinePlayer subject = Bukkit.getOfflinePlayer(report.getSubject());
            OfflinePlayer issuer = Bukkit.getOfflinePlayer(report.getIssuer());

            Duration timeSince = Duration.between(report.getTimestamp(), Instant.now());

            SerializedItem reportItem = parseItem("report_item");
            reportItem.item()
                .addPlaceHolder("short-id", Component.text(id.toString().substring(0, 8)))
                .addPlaceHolder("long-id", Component.text(id.toString()))
                .addPlaceHolder("subject", Component.text(subject.getName()))
                .addPlaceHolder("issuer", Component.text(issuer.getName()))
                .addPlaceHolder("reason", Component.text(report.getReason()))
                .addPlaceHolder("status", Component.text(report.getStatus().name()))
                .addPlaceHolder("server", Component.text(report.getServer()))
                .addPlaceHolder("time_since", Component.text(DurationUtil.format(timeSince)));
            packItem(reportItem, event -> {
              SimpleInventory reportView = new ReportViewInventory(report, getLocale(), this);
              registerChild(reportView);
              reportView.open((Player) event.getWhoClicked());
            });
          });

          if (reports.size() > REPORTS_PER_INVENTORY) {
            addItem(parseItem("next_item"), event -> {
              offset += REPORTS_PER_INVENTORY;
              buildContent();
            });
          }

          if (offset > 0) {
            addItem(parseItem("back_item"), event -> {
              offset -= REPORTS_PER_INVENTORY;
              buildContent();
            });
          }
        });
  }

  @Override
  public void onUpdate(Report report) {
    reOpenForViewers();
  }

  @Override
  public void onCreate(Report report) {
    reOpenForViewers();
  }

  @Override
  public void onDelete(UUID uuid) {
    reOpenForViewers();
  }
}

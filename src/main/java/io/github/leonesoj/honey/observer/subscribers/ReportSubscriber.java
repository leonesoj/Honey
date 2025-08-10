package io.github.leonesoj.honey.observer.subscribers;

import static io.github.leonesoj.honey.locale.Message.argComponent;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.model.Report;
import io.github.leonesoj.honey.database.data.model.Report.ReportStatus;
import io.github.leonesoj.honey.observer.Observer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ReportSubscriber implements Observer<Report> {

  @Override
  public void onCreate(Report report) {
    Bukkit.getOnlinePlayers().forEach(player -> {
      if (player.hasPermission("honey.management.staff")) {
        Honey.getInstance().getStaffHandler().getSessionController()
            .getOrCreateSession(player.getUniqueId())
            .thenAccept(optional -> optional.ifPresent(staffSession -> {
              if (staffSession.hasReportAlerts()) {
                player.sendMessage(
                    Component.translatable("honey.report.broadcast",
                        argComponent("issuer", Bukkit.getPlayer(report.getIssuer()).getName()),
                        argComponent("subject", Bukkit.getPlayer(report.getSubject()).getName()),
                        argComponent("reason", report.getReason().toUpperCase()),
                        Argument.tagResolver(
                            Placeholder.styling("view-report",
                                ClickEvent.runCommand("reports " + report.getId())))
                    )
                );
              }
            }));
      }
    });
  }

  @Override
  public void onUpdate(Report report) {
    if (report.getStatus().equals(ReportStatus.RESOLVED)) {
      Player issuer = Bukkit.getPlayer(report.getIssuer());
      if (issuer != null) {
        issuer.sendMessage(Component.translatable("honey.report.resolved"));
        issuer.playSound(issuer, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 0.5F);
      }
    }
  }
}

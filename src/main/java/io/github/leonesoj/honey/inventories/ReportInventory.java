package io.github.leonesoj.honey.inventories;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.commands.essentials.player.ReportCommand;
import io.github.leonesoj.honey.utils.inventory.SerializedInventory;
import io.github.leonesoj.honey.utils.inventory.SerializedItem;
import io.github.leonesoj.honey.utils.inventory.SimpleInventory;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ReportInventory extends SerializedInventory {

  public final UUID subject;
  public final UUID issuer;

  public ReportInventory(UUID subject, UUID issuer, Locale locale) {
    super(Honey.getInstance(),
        Honey.getInstance().getTranslationHandler().findBestTranslation("report", locale),
        locale,
        null
    );
    this.subject = subject;
    this.issuer = issuer;

    registerChild(new Category(
        getRootSection().getConfigurationSection("hacking_category_gui"),
        locale,
        this
    ));
    registerChild(new Category(
        getRootSection().getConfigurationSection("chat_category_gui"),
        locale,
        this
    ));
    registerChild(new Category(
        getRootSection().getConfigurationSection("other_category_gui"),
        locale,
        this
    ));
  }

  @Override
  protected void buildContent() {
    addItem(parseItem("hacking_category"),
        event -> openChild((Player) event.getWhoClicked(), 0));

    addItem(parseItem("chat_category"),
        event -> openChild((Player) event.getWhoClicked(), 1));

    addItem(parseItem("other_category"),
        event -> openChild((Player) event.getWhoClicked(), 2));
  }

  private final class Category extends SerializedInventory {

    public Category(ConfigurationSection section, Locale locale, SimpleInventory parent) {
      super(Honey.getInstance(), section, locale, parent);
    }

    @Override
    protected void buildContent() {
      addItem(parseItem("cancel_item"),
          event -> event.getWhoClicked().closeInventory());

      addItem(parseItem("back_item"),
          event -> goToParent((Player) event.getWhoClicked()));

      addItem(parseItem("information_item"), null);

      SerializedItem subjectHead = parseItem("subject_head");
      Player subjectPlayer = Bukkit.getPlayer(subject);
      subjectHead.item()
          .asPlayerHead(subjectPlayer, subjectPlayer.getPlayerProfile())
          .addPlaceHolder(builder ->
              builder.matchLiteral("%subject_name%").replacement(subjectPlayer.getName()));
      addItem(subjectHead, null);

      ConfigurationSection reportItems = getItemSection("report_items");
      reportItems.getKeys(false).forEach(key -> {
        SerializedItem item = parseItem(reportItems);

        addItem(item, event -> {
          Player player = (Player) event.getWhoClicked();
          player.closeInventory();

          Honey.getInstance().getDataHandler().getReportController().createReport(
              issuer,
              subject,
              item.otherData().getOrDefault("reason", "unspecified").toString()
          );
          long cooldown =
              Honey.getInstance().getConfig().getInt("reports.report_cooldown", 900) * 1000L;
          ReportCommand.getCooldownService().addPlayer(player.getUniqueId(), cooldown);
          player.sendMessage(Component.translatable("honey.report.submitted"));
        });
      });
    }
  }
}

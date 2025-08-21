package io.github.leonesoj.honey.commands.management;

import static io.github.leonesoj.honey.locale.Message.argComponent;
import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.data.controller.ProfileController;
import io.github.leonesoj.honey.database.data.model.PlayerProfile;
import io.github.leonesoj.honey.utils.other.DurationUtil;
import io.github.leonesoj.honey.utils.other.OfflinePlayerUtil;
import io.github.leonesoj.honey.utils.other.SchedulerUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WhoIsCommand {

  private static final DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern("MM/dd/yyyy hh:mm a z");

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("whois")
        .requires(stack -> stack.getSender() instanceof Player sender
            && sender.hasPermission("honey.management.whois"))
        .then(Commands.argument("player", StringArgumentType.word())
            .suggests(WhoIsCommand::getSuggestions)
            .executes(WhoIsCommand::commandUsage))
        .build();
  }

  private static int commandUsage(CommandContext<CommandSourceStack> ctx) {
    String target = StringArgumentType.getString(ctx, "player");
    UUID playerUUID = ((Player) ctx.getSource().getSender()).getUniqueId();

    OfflinePlayerUtil.getAsyncOfflinePlayer(target, offlinePlayer -> {
      ProfileController controller = Honey.getInstance().getDataHandler().getProfileController();

      controller.getPlayerProfile(offlinePlayer.getUniqueId())
          .thenAccept(optional -> SchedulerUtil.getPlayerScheduler(playerUUID, player -> {
            if (optional.isPresent()) {
              PlayerProfile profile = optional.get();

              ZonedDateTime firstSeen = profile.getFirstSeen().atZone(ZoneId.systemDefault());
              Instant lastSeen = offlinePlayer.isOnline() ? Instant.now() : profile.getLastSeen();
              ZonedDateTime actualLastSeen = lastSeen.atZone(ZoneId.systemDefault());
              Duration playtime = profile.getPlayTime()
                  .plus(controller.getCalculatedPlayTime(profile.getUuid()));

              player.sendMessage(Component.translatable("honey.whois",
                      argComponent("name", offlinePlayer.getName()),
                      argComponent("uuid", profile.getUuid()),
                      argComponent("first-seen", firstSeen.format(formatter)),
                      argComponent("last-seen", actualLastSeen.format(formatter)),
                      argComponent("playtime", DurationUtil.format(playtime)),
                      argComponent("last-connected", profile.getLastConnected())
                  )
              );
            } else {
              player.sendMessage(prefixed("honey.whois.failed"));
            }
          }));
    });
    return Command.SINGLE_SUCCESS;
  }

  private static CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
    Bukkit.getOnlinePlayers().forEach(player -> builder.suggest(player.getName()));
    return builder.buildFuture();
  }
}

package io.github.leonesoj.honey;

import io.github.leonesoj.honey.commands.essentials.gamemode.AdventureCommand;
import io.github.leonesoj.honey.commands.essentials.gamemode.CreativeCommand;
import io.github.leonesoj.honey.commands.essentials.gamemode.SurvivalCommand;
import io.github.leonesoj.honey.commands.essentials.item.LoreCommand;
import io.github.leonesoj.honey.commands.essentials.item.MoreCommand;
import io.github.leonesoj.honey.commands.essentials.item.RenameCommand;
import io.github.leonesoj.honey.commands.essentials.item.RepairCommand;
import io.github.leonesoj.honey.commands.essentials.item.SkullCommand;
import io.github.leonesoj.honey.commands.essentials.player.FeedCommand;
import io.github.leonesoj.honey.commands.essentials.player.FlyCommand;
import io.github.leonesoj.honey.commands.essentials.player.GodCommand;
import io.github.leonesoj.honey.commands.essentials.player.HealCommand;
import io.github.leonesoj.honey.commands.essentials.player.PingCommand;
import io.github.leonesoj.honey.commands.essentials.player.ReportCommand;
import io.github.leonesoj.honey.commands.essentials.player.RulesCommand;
import io.github.leonesoj.honey.commands.essentials.player.SettingsCommand;
import io.github.leonesoj.honey.commands.essentials.player.SpeedCommand;
import io.github.leonesoj.honey.commands.essentials.world.DayCommand;
import io.github.leonesoj.honey.commands.essentials.world.NetherCommand;
import io.github.leonesoj.honey.commands.essentials.world.NightCommand;
import io.github.leonesoj.honey.commands.essentials.world.OverworldCommand;
import io.github.leonesoj.honey.commands.essentials.world.TheEndCommand;
import io.github.leonesoj.honey.commands.management.HoneyCommand;
import io.github.leonesoj.honey.commands.management.ReportsCommand;
import io.github.leonesoj.honey.commands.management.WhoIsCommand;
import io.github.leonesoj.honey.commands.messaging.ChannelCommand;
import io.github.leonesoj.honey.commands.messaging.IgnoreCommand;
import io.github.leonesoj.honey.commands.messaging.MessageCommand;
import io.github.leonesoj.honey.commands.messaging.ReplyCommand;
import io.github.leonesoj.honey.commands.messaging.UnignoreCommand;
import io.github.leonesoj.honey.commands.moderation.ChatCommand;
import io.github.leonesoj.honey.commands.moderation.InvseeCommand;
import io.github.leonesoj.honey.commands.moderation.StaffCommand;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.Set;

public class HoneyBootstrap implements PluginBootstrap {

  @Override
  public void bootstrap(BootstrapContext context) {
    context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
      /* PLAYER COMMANDS */
      commands.registrar().register(AdventureCommand.create());
      commands.registrar().register(CreativeCommand.create());
      commands.registrar().register(SurvivalCommand.create());
      commands.registrar().register(FlyCommand.create());
      commands.registrar().register(GodCommand.create());
      commands.registrar().register(FeedCommand.create());
      commands.registrar().register(HealCommand.create());
      commands.registrar().register(SettingsCommand.create());
      commands.registrar().register(ReportCommand.create());
      commands.registrar().register(SpeedCommand.create());
      commands.registrar().register(PingCommand.create());
      commands.registrar().register(InvseeCommand.create());
      commands.registrar().register(RulesCommand.create());

      /* ITEM COMMANDS */
      commands.registrar().register(LoreCommand.create());
      commands.registrar().register(MoreCommand.create());
      commands.registrar().register(RenameCommand.create());
      commands.registrar().register(RepairCommand.create());
      commands.registrar().register(SkullCommand.create());

      /* WORLD COMMANDS */
      commands.registrar().register(NetherCommand.create());
      commands.registrar().register(OverworldCommand.create());
      commands.registrar().register(TheEndCommand.create());
      commands.registrar().register(DayCommand.create());
      commands.registrar().register(NightCommand.create());

      /* MESSAGING COMMANDS */
      commands.registrar().register(MessageCommand.create(), "", Set.of("msg", "tell", "w"));
      commands.registrar().register(ReplyCommand.create(), "", Set.of("r"));
      commands.registrar().register(IgnoreCommand.create());
      commands.registrar().register(UnignoreCommand.create());
      commands.registrar().register(ChannelCommand.create());

      /* MANAGEMENT COMMANDS */
      commands.registrar().register(HoneyCommand.create());
      commands.registrar().register(ReportsCommand.create());
      commands.registrar().register(WhoIsCommand.create());

      /* MODERATION COMMANDS */
      commands.registrar().register(StaffCommand.create());
      commands.registrar().register(ChatCommand.create());
    });
  }

}

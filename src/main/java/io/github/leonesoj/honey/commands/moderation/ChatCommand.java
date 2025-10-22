package io.github.leonesoj.honey.commands.moderation;

import static io.github.leonesoj.honey.locale.Message.argComponent;
import static io.github.leonesoj.honey.locale.Message.prefixed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.ChatChannel;
import io.github.leonesoj.honey.chat.ChatService;
import io.github.leonesoj.honey.chat.SpyService;
import io.github.leonesoj.honey.utils.command.DurationArgument;
import io.github.leonesoj.honey.utils.command.OtherPlayerArgument;
import io.github.leonesoj.honey.utils.other.DurationUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ChatCommand {

  public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("chat")
        .requires(stack -> stack.getSender().hasPermission("honey.moderation.chat"))
        .then(Commands.literal("mute").executes(ChatCommand::muteHereUsage))
        .then(Commands.literal("unmute").executes(ChatCommand::unmuteHereUsage))
        .then(Commands.literal("slow").executes(ChatCommand::slowHereUsage)
            .then(Commands.argument("duration", new DurationArgument())
                .executes(ChatCommand::slowDurationUsage)))
        .then(Commands.literal("unslow").executes(ChatCommand::unSlowUsage))
        .then(Commands.literal("clear").executes(ChatCommand::clearHereUsage))
        .then(Commands.literal("mod").executes(ChatCommand::modUsage))
        .then(Commands.literal("spy").executes(ChatCommand::spyUsage)
            .then(Commands.argument("player", new OtherPlayerArgument(true))
                .executes(ChatCommand::spyTargetUsage)))
        .build();
  }

  private static int muteHereUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ChatChannel channel = Honey.getInstance().getChatService().getMemberChannel(sender);

    if (!channel.isMuted()) {
      channel.setMuted(true);

      sender.sendMessage(prefixed("honey.chat.mute",
          argComponent("channel", channel.getIdentifier())
      ));
      channel.getMembers().sendMessage(Component.translatable("honey.chat.mute.announce",
          argComponent("player", sender.getName()),
          argComponent("channel", channel.getIdentifier())
      ));
    } else {
      sender.sendMessage(prefixed("honey.chat.mute.already",
          argComponent("channel", channel.getIdentifier())
      ));
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int unmuteHereUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ChatChannel channel = Honey.getInstance().getChatService().getMemberChannel(sender);
    if (channel.isMuted()) {
      channel.setMuted(false);

      sender.sendMessage(prefixed("honey.chat.unmute",
          argComponent("channel", channel.getIdentifier())
      ));
      channel.getMembers().sendMessage(Component.translatable("honey.chat.unmute.announce",
          argComponent("player", sender.getName()),
          argComponent("channel", channel.getIdentifier())
      ));
    } else {
      sender.sendMessage(prefixed("honey.chat.unmute.already",
          argComponent("channel", channel.getIdentifier())
      ));
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int slowHereUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ChatChannel channel = Honey.getInstance().getChatService().getMemberChannel(sender);
    if (!channel.isSlowed()) {
      channel.setSlowed();

      sender.sendMessage(prefixed("honey.chat.slow",
          argComponent("channel", channel.getIdentifier()),
          argComponent("duration",
              DurationUtil.format(Duration.ofMillis(channel.getSlowDuration())))
      ));
      channel.getMembers().sendMessage(Component.translatable("honey.chat.slow.announce",
          argComponent("player", sender.getName()),
          argComponent("channel", channel.getIdentifier()),
          argComponent("duration",
              DurationUtil.format(Duration.ofMillis(channel.getSlowDuration())))
      ));
    } else {
      sender.sendMessage(prefixed("honey.chat.slow.already",
          argComponent("channel", channel.getIdentifier())
      ));
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int slowDurationUsage(CommandContext<CommandSourceStack> ctx) {
    Duration duration = ctx.getArgument("duration", Duration.class);
    Player sender = (Player) ctx.getSource().getSender();
    ChatChannel channel = Honey.getInstance().getChatService().getMemberChannel(sender);
    if (!channel.isSlowed()) {
      channel.setSlowed(duration.toMillis());

      sender.sendMessage(prefixed("honey.chat.slow",
          argComponent("channel", channel.getIdentifier()),
          argComponent("duration",
              DurationUtil.format(duration))
      ));
      channel.getMembers().sendMessage(Component.translatable("honey.chat.slow.announce",
          argComponent("player", sender.getName()),
          argComponent("channel", channel.getIdentifier()),
          argComponent("duration",
              DurationUtil.format(duration))
      ));
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int unSlowUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ChatChannel channel = Honey.getInstance().getChatService().getMemberChannel(sender);
    if (channel.isSlowed()) {
      channel.setUnSlowed();

      sender.sendMessage(prefixed("honey.chat.unslow",
          argComponent("channel", channel.getIdentifier())
      ));
      channel.getMembers().sendMessage(Component.translatable("honey.chat.unslow.announce",
          argComponent("player", sender.getName()),
          argComponent("channel", channel.getIdentifier())
      ));
    } else {
      sender.sendMessage(prefixed("honey.chat.unslow.already",
          argComponent("channel", channel.getIdentifier())
      ));
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int clearHereUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ChatChannel channel = Honey.getInstance().getChatService().getMemberChannel(sender);

    ForwardingAudience audience = channel.getMembers();
    Audience filteredAudience = audience.filterAudience(
        aud -> !(aud instanceof ConsoleCommandSender)
    );
    for (int i = 0; i < 100; i++) {
      filteredAudience.sendMessage(Component.empty());
    }

    sender.sendMessage(prefixed("honey.chat.clear",
        argComponent("channel", channel.getIdentifier())
    ));
    filteredAudience.sendMessage(Component.translatable("honey.chat.clear.announce",
        argComponent("player", sender.getName()),
        argComponent("channel", channel.getIdentifier())
    ));
    return Command.SINGLE_SUCCESS;
  }

  private static int modUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    ChatService chatService = Honey.getInstance().getChatService();

    if (!chatService.isChatMod(sender.getUniqueId())) {
      chatService.setChatModStatus(sender.getUniqueId(), true);
      sender.sendMessage(prefixed("honey.chat.mod.enabled"));
    } else {
      chatService.setChatModStatus(sender.getUniqueId(), false);
      sender.sendMessage(prefixed("honey.chat.mod.disabled"));
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int spyUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();

    boolean newStatus = Honey.getInstance().getChatService()
        .getSpyService()
        .toggleGlobalSpy(sender.getUniqueId());
    if (newStatus) {
      sender.sendMessage(prefixed("honey.socialspy.enabled"));
    } else {
      sender.sendMessage(prefixed("honey.socialspy.disabled"));
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int spyTargetUsage(CommandContext<CommandSourceStack> ctx) {
    Player sender = (Player) ctx.getSource().getSender();
    Player target = ctx.getArgument("player", Player.class);

    UUID senderId = sender.getUniqueId();
    UUID targetId = target.getUniqueId();

    SpyService spyService = Honey.getInstance().getChatService().getSpyService();

    Set<UUID> currentTargets = spyService.getTargetsOfSpy(senderId);
    if (!currentTargets.contains(targetId)) {
      spyService.trackAsSpy(senderId, targetId);
      sender.sendMessage(prefixed("honey.spy.enabled",
          argComponent("player", target.getName()))
      );
    } else {
      spyService.untrackSpyTarget(senderId, targetId);
      sender.sendMessage(prefixed("honey.spy.disabled",
          argComponent("player", target.getName()))
      );
    }
    return Command.SINGLE_SUCCESS;
  }
}

package io.github.leonesoj.honey.utils.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.chat.ChatChannel;
import io.github.leonesoj.honey.chat.ChatService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ChatChannelArgument implements CustomArgumentType.Converted<ChatChannel, String> {

  private static final SimpleCommandExceptionType ERROR_BAD_SOURCE = new SimpleCommandExceptionType(
      MessageComponentSerializer.message()
          .serialize(Component.text("The source needs to be a CommandSourceStack!"))
  );

  private static final SimpleCommandExceptionType ERROR_NO_PLAYER = new SimpleCommandExceptionType(
      MessageComponentSerializer.message()
          .serialize(Component.text("The sender needs to be a Player"))
  );

  private static final SimpleCommandExceptionType ERROR_NE_CHANNEL = new SimpleCommandExceptionType(
      MessageComponentSerializer.message().serialize(
          Component.translatable("honey.command.channel.invalid")
      )
  );

  private static final SimpleCommandExceptionType ERROR_NP_CHANNEL = new SimpleCommandExceptionType(
      MessageComponentSerializer.message().serialize(
          Component.translatable("honey.command.channel.disallowed")
      )
  );

  @Override
  public ChatChannel convert(String nativeType) {
    throw new UnsupportedOperationException("This method should never be called");
  }

  @Override
  public <S> ChatChannel convert(String channelArgument, S source) throws CommandSyntaxException {
    if (!(source instanceof CommandSourceStack stack)) {
      throw ERROR_BAD_SOURCE.create();
    }
    if (!(stack.getSender() instanceof Player sender)) {
      throw ERROR_NO_PLAYER.create();
    }

    ChatService chatService = Honey.getInstance().getChatService();
    Set<String> channelIds = chatService.getChannels().stream()
        .map(ChatChannel::getIdentifier).collect(Collectors.toSet());

    if (!channelIds.contains(channelArgument.toLowerCase(Locale.ROOT))) {
      throw ERROR_NE_CHANNEL.create();
    }

    ChatChannel channel = chatService.getChannel(channelArgument);
    if (!channel.canJoin(sender)) {
      throw ERROR_NP_CHANNEL.create();
    }

    return channel;
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.word();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
      SuggestionsBuilder builder) {
    CommandSourceStack stack = (CommandSourceStack) context.getSource();
    Player player = (Player) stack.getSender();
    Honey.getInstance().getChatService().getChannels().stream()
        .filter(chatChannel -> chatChannel.canJoin(player))
        .map(ChatChannel::getIdentifier)
        .forEach(builder::suggest);
    return builder.buildFuture();
  }
}

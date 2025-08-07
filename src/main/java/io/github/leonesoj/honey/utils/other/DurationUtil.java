package io.github.leonesoj.honey.utils.other;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationUtil {

  private static final Pattern DURATION_PATTERN = Pattern.compile(
      "(?:(\\d+)d)?\\s*(?:(\\d+)h)?\\s*(?:(\\d+)m)?\\s*(?:(\\d+)s)?",
      Pattern.CASE_INSENSITIVE
  );

  private DurationUtil() {
  }

  public static Duration parse(String input) {
    Matcher matcher = DURATION_PATTERN.matcher(input.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid duration format: " + input);
    }

    long days = parseNumber(matcher.group(1));
    long hours = parseNumber(matcher.group(2));
    long minutes = parseNumber(matcher.group(3));
    long seconds = parseNumber(matcher.group(4));

    return Duration.ofDays(days)
        .plusHours(hours)
        .plusMinutes(minutes)
        .plusSeconds(seconds);
  }

  public static String format(Duration duration) {
    long totalMillis = duration.toMillis();

    final long days = totalMillis / 86_400_000;
    totalMillis %= 86_400_000;

    final long hours = totalMillis / 3_600_000;
    totalMillis %= 3_600_000;

    final long minutes = totalMillis / 60_000;
    totalMillis %= 60_000;

    final long seconds = totalMillis / 1_000;
    final long millis = totalMillis % 1_000;

    StringBuilder sb = new StringBuilder();
    if (days > 0) {
      sb.append(days).append("d ");
    }

    if (hours > 0) {
      sb.append(hours).append("h ");
    }

    if (minutes > 0) {
      sb.append(minutes).append("m ");
    }

    if (seconds > 0 || millis > 0 || sb.isEmpty()) {
      double totalSeconds = seconds + millis / 1000.0;
      sb.append(String.format("%.2fs", totalSeconds));
    }

    return sb.toString().trim();
  }


  private static long parseNumber(String group) {
    return group == null ? 0 : Long.parseLong(group);
  }
}


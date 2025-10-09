package io.github.leonesoj.honey.chat.filtering;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;

public final class ChatFilter {

  private final ForbiddenWords forbidden;

  private static final Set<String> TLD_SET = Set.of(
      "com", "net", "org", "edu", "gov", "mil", "int",
      "info", "biz", "name", "pro", "aero", "coop", "museum",
      "app", "dev", "xyz", "online", "site", "shop", "blog", "io"
  );

  private static final Pattern DOMAIN_LIKE = Pattern.compile(
      "(?i)\\b(?:(?:https?|ftp)://)?(?:www\\.)?"
          + "(?<host>(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+" + "(?<tld>[a-z]{2,63}))"
          + "(?::\\d{2,5})?(?:/[\\w./?%#&=+~;,-]*)?\\b"
  );

  private static final Pattern IP_PATTERN = Pattern.compile("\\b(\\d{1,3})(?:\\.(\\d{1,3})){3}\\b");
  private static final Pattern BAD_CHARS = Pattern.compile(
      "[" + "\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F-\\u009F"
          + "\\u200B\\u200C\\u200D\\u200E\\u200F" + "\\u202A-\\u202E\\u2066-\\u2069"
          + "\\uFE0E\\uFE0F\\uFEFF" + "\\uFFFC" + "\\u5350\\u534D" + "]"
  );

  public ChatFilter(ForbiddenWords forbidden) {
    this.forbidden = forbidden;
  }

  public enum Action {
    ALLOW, CENSOR, BLOCK_WORD, BLOCK_URL, BLOCK_IP, BLOCK_BAD_CHAR
  }

  public record Result(Action action, Component censoredText) {

  }

  public Result apply(String input) {
    Component inputComponent = Component.text(input);

    if (BAD_CHARS.matcher(input).find()) {
      return new Result(Action.BLOCK_BAD_CHAR, inputComponent);
    }
    if (containsDomainWithKnownTld(input)) {
      return new Result(Action.BLOCK_URL, inputComponent);
    }

    if (containsIp(input)) {
      return new Result(Action.BLOCK_IP, inputComponent);
    }

    Pattern blockPat = forbidden.blockPattern();
    if (blockPat != null && blockPat.matcher(input).find()) {
      return new Result(Action.BLOCK_WORD, inputComponent);
    }

    String censored = censorForbidden(input, forbidden.censorPattern());
    if (!censored.equals(input)) {
      return new Result(Action.CENSOR, Component.text(censored));
    }

    return new Result(Action.ALLOW, inputComponent);
  }

  private static boolean containsDomainWithKnownTld(String s) {
    Matcher m = DOMAIN_LIKE.matcher(s);
    while (m.find()) {
      String tld = m.group("tld");
      if (tld != null && TLD_SET.contains(tld.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsIp(String s) {
    Matcher m = IP_PATTERN.matcher(s);
    while (m.find()) {
      String[] parts = m.group().split("\\.");
      boolean ok = true;
      for (String part : parts) {
        int n = Integer.parseInt(part);
        if (n < 0 || n > 255) {
          ok = false;
          break;
        }
      }
      if (ok) {
        return true;
      }
    }
    return false;
  }

  private static String censorForbidden(String s, Pattern compiled) {
    if (compiled == null) {
      return s;
    }

    Matcher matcher = compiled.matcher(s);
    StringBuilder sb = new StringBuilder(Math.max(16, s.length()));

    boolean found = false;
    while (matcher.find()) {
      found = true;
      String stars = "*".repeat(matcher.end() - matcher.start());
      matcher.appendReplacement(sb, stars);
    }
    if (!found) {
      return s;
    }

    matcher.appendTail(sb);
    return sb.toString();
  }
}

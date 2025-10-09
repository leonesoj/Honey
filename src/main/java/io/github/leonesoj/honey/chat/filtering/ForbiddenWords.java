package io.github.leonesoj.honey.chat.filtering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.plugin.java.JavaPlugin;

public final class ForbiddenWords {

  private final Pattern censorPattern;
  private final Pattern blockPattern;

  private static final String CENSOR_FILE = "censored-words.txt";
  private static final String BLOCK_FILE = "blocked-words.txt";

  private ForbiddenWords(Pattern censorPattern, Pattern blockPattern) {
    this.censorPattern = censorPattern;
    this.blockPattern = blockPattern;
  }

  public static ForbiddenWords load(JavaPlugin plugin) {
    Set<String> censorLoaded = loadWordSet(plugin, CENSOR_FILE);
    Set<String> blockLoaded = loadWordSet(plugin, BLOCK_FILE);

    if (censorLoaded == null) {
      censorLoaded = Collections.emptySet();
    }
    if (blockLoaded == null) {
      blockLoaded = Collections.emptySet();
    }

    Set<String> censUnmod = Collections.unmodifiableSet(censorLoaded);
    Set<String> blocUnmod = Collections.unmodifiableSet(blockLoaded);

    Pattern censorPat = buildLeetAwarePattern(censUnmod);
    Pattern blockPat = buildLeetAwarePattern(blocUnmod);

    plugin.getLogger()
        .info("Forbidden words loaded: censor=" + censUnmod.size() + ", block=" + blocUnmod.size());
    return new ForbiddenWords(censorPat, blockPat);
  }

  public Pattern censorPattern() {
    return censorPattern;
  }

  public Pattern blockPattern() {
    return blockPattern;
  }

  private static Set<String> loadWordSet(JavaPlugin plugin, String fileName) {
    Set<String> loaded = null;
    try {
      Path dataDir = plugin.getDataFolder().toPath();
      Files.createDirectories(dataDir);
      Path fp = dataDir.resolve(fileName);

      if (Files.notExists(fp)) {
        plugin.saveResource(fileName, false);
        plugin.getLogger().info("Created " + fileName + " from plugin resources.");
      }

      if (Files.exists(fp)) {
        loaded = readWordsFromPath(fp);
      }
    } catch (Throwable t) {
      plugin.getLogger()
          .warning("Failed to read " + fileName + " from data folder: " + t.getMessage());
    }

    if (loaded == null) {
      try (InputStream in = plugin.getResource(fileName)) {
        if (in != null) {
          loaded = readWordsFromStream(in);
          plugin.getLogger().info("Loaded " + fileName + " from plugin resources (fallback).");
        }
      } catch (Throwable t) {
        plugin.getLogger()
            .severe("Failed to read " + fileName + " from plugin resources: " + t.getMessage());
      }
    }

    return loaded;
  }

  private static Set<String> readWordsFromPath(Path fp) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(fp, StandardCharsets.UTF_8)) {
      return readWordsFromReader(br);
    }
  }

  private static Set<String> readWordsFromStream(InputStream in) throws IOException {
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(in, StandardCharsets.UTF_8))) {
      return readWordsFromReader(br);
    }
  }

  private static Set<String> readWordsFromReader(BufferedReader br) throws IOException {
    Set<String> out = new LinkedHashSet<>();
    for (String line; (line = br.readLine()) != null; ) {
      String t = line.trim();
      if (t.isEmpty() || t.startsWith("#")) {
        continue;
      }
      out.add(t.toLowerCase(Locale.ROOT));
    }
    return out;
  }

  private static String charClass(char c) {
    return switch (Character.toLowerCase(c)) {
      case 'a' -> "[a4@]";
      case 'b' -> "[b8]";
      case 'e' -> "[e3]";
      case 'g' -> "[g9]";
      case 'i' -> "[i1!|]";
      case 'l' -> "[l1|]";
      case 'o' -> "[o0]";
      case 's' -> "[s5$]";
      case 't' -> "[t7+]";
      case 'z' -> "[z2]";
      default -> Pattern.quote(String.valueOf(c));
    };
  }

  private static final String SEP = "(?:[^\\p{L}\\p{Nd}]{0,2})";

  private static String wordToLeetRegex(String w) {
    StringBuilder sb = new StringBuilder(w.length() * 6);
    boolean first = true;
    for (int i = 0; i < w.length(); i++) {
      char ch = w.charAt(i);
      if (!first) {
        sb.append(SEP);
      }
      if (Character.isLetterOrDigit(ch)) {
        sb.append(charClass(ch));
      } else {
        sb.append(SEP);
      }
      first = false;
    }
    return "\\b(?:" + sb + ")\\b";
  }

  private static Pattern buildLeetAwarePattern(Set<String> words) {
    if (words.isEmpty()) {
      return Pattern.compile("(?!x)x");
    }
    String alternation = words.stream()
        .sorted(Comparator.comparingInt(String::length).reversed())
        .map(ForbiddenWords::wordToLeetRegex)
        .collect(Collectors.joining("|"));
    return Pattern.compile("(?iu)(?:" + alternation + ")");
  }
}

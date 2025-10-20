package io.github.leonesoj.honey.secret;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bukkit.plugin.java.JavaPlugin;

public class SecretHandler {

  private static final String SECRET_PATH = "secret";
  private static final String ALGORITHM = "HmacSHA256";

  private final JavaPlugin plugin;

  public SecretHandler(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void generateSecret() {
    if (plugin.getConfig().getString(SECRET_PATH, "").isEmpty()) {
      byte[] pepperBytes = new byte[32];
      new SecureRandom().nextBytes(pepperBytes);
      plugin.getConfig().set(SECRET_PATH, Base64.getEncoder().encodeToString(pepperBytes));
      plugin.saveConfig();
    }
  }

  private byte[] hmacBytes(String input) throws Exception {
    String pepper = plugin.getConfig().getString(SECRET_PATH, "");
    byte[] pepperBytes = Base64.getDecoder().decode(pepper.trim());
    Mac mac = Mac.getInstance(ALGORITHM);
    mac.init(new SecretKeySpec(pepperBytes, ALGORITHM));
    return mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
  }

  public byte[] hash(String input) {
    try {
      return hmacBytes(input);
    } catch (Exception exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to run hash function", exception);
      throw new IllegalStateException("Failed to run hash function", exception);
    }
  }

  public boolean verifyHash(String actualBase64, String expectedBase64) {
    byte[] expected = Base64.getDecoder().decode(expectedBase64.trim());
    try {
      byte[] actual = hmacBytes(actualBase64);
      return MessageDigest.isEqual(actual, expected);
    } catch (Exception exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to verify two hashes", exception);
      return false;
    }
  }

}

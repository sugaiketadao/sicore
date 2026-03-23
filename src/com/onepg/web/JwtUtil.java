package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JWT utility class.
 * <ul>
 *   <li>Generates and validates JSON Web Tokens.</li>
 *   <li>Uses HMAC-SHA256 (HS256) as the signing algorithm.</li>
 * </ul>
 * @hidden
 */
final class JwtUtil {

  /** Signing algorithm. */
  private static final String SIGN_ALG = "HmacSHA256";
  /** JWT header (Base64URL encoded). */
  private static final String JWT_HEADER = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
  /** Signing secret key. */
  private static final String SECRET_KEY = ServerUtil.PROP_MAP.getStringOrDefault("jwt.secret.key", "must-be-configured-in-web.properties");
  /** Expiration period (seconds). */
  private static final long EXPIRE_SEC = ServerUtil.PROP_MAP.getLongOrDefault("jwt.expire.sec", 86_400L); // Default is 24 hours

  /**
   * Constructor.
   */
  private JwtUtil() {
    // No-op
  }

  /**
   * Generates a JWT.
   *
   * @param id sign-in ID
   * @return the JWT string
   */
  static String createToken(final String id) {
    final long now = System.currentTimeMillis() / 1_000L;
    final String payloadJson = "{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d}".formatted(id, now, now + EXPIRE_SEC);
    final String payload = base64UrlEncode(payloadJson);
    final String signData = JWT_HEADER + "." + payload;
    return signData + "." + sign(signData);
  }

  /**
   * Validates a JWT.
   *
   * @param token JWT string
   * @return the sign-in ID
   */
  static String validateToken(final String token) {
    if (ValUtil.isBlank(token)) {
      throw new RuntimeException("JWT token is blank.");
    }
    // Token for error logging (partially masked)
    final String errToken = ValUtil.substring(token, 0, 1) + "***";

    final String[] parts = token.split("\\.", -1);
    if (parts.length != 3) {
      throw new RuntimeException("JWT format is invalid." + LogUtil.joinKeyVal("token", errToken));
    }
    // Signature validation
    final String signData = parts[0] + "." + parts[1];
    final String signed = sign(signData);
    if (!signed.equals(parts[2])) {
      throw new RuntimeException("JWT signature is invalid." + LogUtil.joinKeyVal("token", errToken));
    }
    // Payload validation (expiration)
    final String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    final long exp = getExpField(json, errToken);
    if (System.currentTimeMillis() / 1_000L > exp) {
      throw new RuntimeException("JWT token is expired." + LogUtil.joinKeyVal("token", errToken));
    }
    return getSubField(json, errToken);
  }

  /**
   * Generates an HMAC-SHA256 signature.
   *
   * @param data data to sign
   * @return the Base64URL encoded signature
   */
  private static String sign(final String data) {
    try {
      final Mac mac = Mac.getInstance(SIGN_ALG);
      final SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), SIGN_ALG);
      mac.init(keySpec);
      final byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    } catch (final Exception e) {
      throw new RuntimeException("JWT signature generation failed.", e);
    }
  }

  /**
   * Retrieves the sub field from the payload.
   *
   * @param payloadJson payload JSON string
   * @param errToken token for error logging (partially masked)
   * @return the sub field value (sign-in ID)
   */
  private static String getSubField(final String payloadJson, final String errToken) {
    final String key = "\"sub\":\"";
    final int start = payloadJson.indexOf(key);
    if (start < 0) {
      throw new RuntimeException("JWT payload 'sub' field not found." + LogUtil.joinKeyVal("token", errToken));
    }
    final int fStart = start + key.length();
    final int fEnd = payloadJson.indexOf("\"", fStart);
    if (fEnd < 0) {
      throw new RuntimeException("JWT payload 'sub' field is malformed." + LogUtil.joinKeyVal("token", errToken));
    }
    return payloadJson.substring(fStart, fEnd);
  }

  /**
   * Retrieves the exp field from the payload.
   *
   * @param payloadJson payload JSON string
   * @param errToken token for error logging (partially masked)
   * @return the exp field value (expiration time)
   */
  private static long getExpField(final String payloadJson, final String errToken) {
    final String key = "\"exp\":";
    final int start = payloadJson.indexOf(key);
    if (start < 0) {
      throw new RuntimeException("JWT payload 'exp' field not found." + LogUtil.joinKeyVal("token", errToken));
    }
    final int fStart = start + key.length();
    int fEnd = fStart;
    while (fEnd < payloadJson.length() && Character.isDigit(payloadJson.charAt(fEnd))) {
      fEnd++;
    }
    return Long.parseLong(payloadJson.substring(fStart, fEnd));
  }
  
  /**
   * Converts to Base64URL encoding.<br>
   * <ul>
   * <li>Encodes to Base64URL format without trailing padding characters (<code>=</code>), conforming to the JWT specification (RFC 7515).</li>
   * </ul>
   *
   * @param value the string to convert
   * @return the converted string
   */
  private static String base64UrlEncode(final String value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}

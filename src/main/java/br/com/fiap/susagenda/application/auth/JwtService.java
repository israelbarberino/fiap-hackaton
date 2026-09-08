package br.com.fiap.susagenda.application.auth;

import br.com.fiap.susagenda.domain.user.User;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class JwtService {
    private static final String ALGORITHM = "HmacSHA256";
    private static final long TOKEN_DURATION_SECONDS = 3600;
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("\\\"sub\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\\\"role\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern EXPIRATION_PATTERN = Pattern.compile("\\\"exp\\\":(\\d+)");
    private final String secret = System.getenv().getOrDefault("JWT_SECRET", "local-development-secret-change-me");

    public String issue(User user) {
        long expiresAt = Instant.now().getEpochSecond() + TOKEN_DURATION_SECONDS;
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + escape(user.id()) + "\",\"role\":\"" + user.role()
                + "\",\"exp\":" + expiresAt + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public Map<String, String> verify(String token) {
        try {
            token = token.trim();
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Map.of();
            }
            if (!sign(parts[0] + "." + parts[1]).equals(parts[2])) {
                return Map.of();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long expiration = Long.parseLong(extract(payload, EXPIRATION_PATTERN));
            if (expiration <= Instant.now().getEpochSecond()) {
                return Map.of();
            }
            Map<String, String> claims = new HashMap<>();
            claims.put("sub", extract(payload, SUBJECT_PATTERN));
            claims.put("role", extract(payload, ROLE_PATTERN));
            return claims;
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível assinar o token.", exception);
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String extract(String json, Pattern pattern) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("JWT claim ausente.");
        }
        return matcher.group(1);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

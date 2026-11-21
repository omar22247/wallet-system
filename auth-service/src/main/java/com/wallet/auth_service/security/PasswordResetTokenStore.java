package com.wallet.auth_service.security;

import com.wallet.auth_service.config.PasswordResetProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenStore {

    private static final String TOKEN_KEY = "pwreset:token:";       // + sha256(token) -> userId
    private static final String USER_KEY = "pwreset:user:";         // + userId -> sha256(token)
    private static final String COOLDOWN_KEY = "pwreset:cooldown:"; // + email

    private final StringRedisTemplate redis;
    private final PasswordResetProperties props;
    private final SecureRandom random = new SecureRandom();

    public boolean tryAcquireCooldown(String email) {
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(COOLDOWN_KEY + email, "1", props.resendCooldown()));
    }

    public String issue(UUID userId) {
        String previous = redis.opsForValue().get(USER_KEY + userId);
        if (previous != null) redis.delete(TOKEN_KEY + previous);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = sha256(token);

        redis.opsForValue().set(TOKEN_KEY + hash, userId.toString(), props.ttl());
        redis.opsForValue().set(USER_KEY + userId, hash, props.ttl());
        return token;
    }

    public Optional<UUID> consume(String token) {
        String hash = sha256(token);
        String userId = redis.opsForValue().getAndDelete(TOKEN_KEY + hash);
        if (userId == null) return Optional.empty();
        redis.delete(USER_KEY + userId);
        return Optional.of(UUID.fromString(userId));
    }

    public void revoke(UUID userId) {
        String hash = redis.opsForValue().getAndDelete(USER_KEY + userId);
        if (hash != null) redis.delete(TOKEN_KEY + hash);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
package com.wallet.auth_service.security;

import com.wallet.auth_service.config.OtpProperties;
import com.wallet.auth_service.exception.OtpRequestLimitException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;


@Component
public class OtpStore {

    private static final String CODE_KEY = "otp:register:code:";
    private static final String ATTEMPTS_KEY = "otp:register:attempts:";
    private static final String COOLDOWN_KEY = "otp:register:cooldown:";
    private static final String COUNT_KEY = "otp:register:count:";
    private static final Duration COUNT_WINDOW = Duration.ofHours(1);

    private final StringRedisTemplate redis;
    private final OtpProperties props;
    private final SecureRandom random = new SecureRandom();

    public OtpStore(StringRedisTemplate redis, OtpProperties props) {
        this.redis = redis;
        this.props = props;
    }

    public String issue(String email) {
        Boolean firstInWindow = redis.opsForValue()
                .setIfAbsent(COOLDOWN_KEY + email, "1", props.resendCooldown());
        if (!Boolean.TRUE.equals(firstInWindow)) {
            throw new OtpRequestLimitException("Please wait before requesting a new code");
        }
        Long count = redis.opsForValue().increment(COUNT_KEY + email);
        if (count != null && count == 1) {
            redis.expire(COUNT_KEY + email, COUNT_WINDOW);
        }
        if (count != null && count > props.maxRequestsPerHour()) {
            throw new OtpRequestLimitException("Too many codes requested. Please try again later");
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        redis.opsForValue().set(CODE_KEY + email, hmac(code), props.ttl());
        redis.delete(ATTEMPTS_KEY + email);
        return code;
    }

    public void discard(String email) {
        redis.delete(CODE_KEY + email);
        redis.delete(ATTEMPTS_KEY + email);
    }

    public boolean verify(String email, String code) {
        String stored = redis.opsForValue().get(CODE_KEY + email);
        if (stored == null) {
            return false;
        }

        Long attempts = redis.opsForValue().increment(ATTEMPTS_KEY + email);
        if (attempts != null && attempts == 1) {
            redis.expire(ATTEMPTS_KEY + email, props.ttl());
        }
        if (attempts != null && attempts > props.maxAttempts()) {
            discard(email);
            return false;
        }

        boolean matches = MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8),
                hmac(code).getBytes(StandardCharsets.UTF_8));

        if (matches) {
            discard(email);
        }
        return matches;
    }
    public void rollbackIssue(String email) {
        redis.delete(CODE_KEY + email);
        redis.delete(ATTEMPTS_KEY + email);
        redis.delete(COOLDOWN_KEY + email);
        redis.opsForValue().decrement(COUNT_KEY + email);
    }
    private String hmac(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(code.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }
}
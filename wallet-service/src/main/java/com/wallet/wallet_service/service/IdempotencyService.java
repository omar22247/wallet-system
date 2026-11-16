package com.wallet.wallet_service.service;

import com.wallet.wallet_service.entity.IdempotencyRecord;
import com.wallet.wallet_service.exception.IdempotencyKeyReusedException;
import com.wallet.wallet_service.exception.InvalidIdempotencyKeyException;
import com.wallet.wallet_service.exception.RequestAlreadyProcessedException;
import com.wallet.wallet_service.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public void validateKey(UUID key) {
        if (key.version() != 4) {
            throw new InvalidIdempotencyKeyException();
        }
    }

    public void ensureNotProcessed(UUID actorId, UUID key, String requestHash) {
        repository.findById(new IdempotencyRecord.Key(actorId, key)).ifPresent(previous -> {            if (previous.getRequestHash().equals(requestHash)) {
                throw new RequestAlreadyProcessedException();
            }
            throw new IdempotencyKeyReusedException();
        });
    }

    public void markProcessed(UUID actorId, UUID key, String requestHash) {
        repository.save(IdempotencyRecord.of(actorId, key, requestHash));
    }

    public String hash(String operation, Object... parts) {
        StringBuilder canonical = new StringBuilder(operation).append('|');
        for (Object part : parts) {
            canonical.append(part == null ? "" : part.toString()).append('|');
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
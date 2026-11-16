package com.wallet.wallet_service.service;
import com.wallet.wallet_service.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class IdempotencyCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupJob.class);
    private static final Duration RETENTION = Duration.ofHours(24);

    private final IdempotencyRecordRepository repository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpired() {
        int deleted = repository.deleteOlderThan(Instant.now().minus(RETENTION));
        if (deleted > 0) {
            log.info("Deleted {} expired idempotency keys", deleted);
        }
    }
}
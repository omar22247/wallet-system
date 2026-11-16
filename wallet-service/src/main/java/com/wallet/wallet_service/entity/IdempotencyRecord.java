package com.wallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys",
        indexes = @Index(name = "idx_idempotency_created", columnList = "created_at"))
@IdClass(IdempotencyRecord.Key.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord {

    @Id
    @Column(name = "actor_id", nullable = false, updatable = false)
    private UUID actorId;

    @Id
    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private UUID idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64, updatable = false)
    private String requestHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public record Key(UUID actorId, UUID idempotencyKey) implements Serializable {}

    public static IdempotencyRecord of(UUID actorId, UUID key, String requestHash) {
        IdempotencyRecord r = new IdempotencyRecord();
        r.actorId = actorId;
        r.idempotencyKey = key;
        r.requestHash = requestHash;
        return r;
    }
}
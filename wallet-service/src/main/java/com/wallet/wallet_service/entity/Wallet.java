package com.wallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true, updatable = false)
    private UUID userId;

    @Column(name = "system_code", unique = true, updatable = false, length = 30)
    private String systemCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private WalletType type;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private WalletStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Wallet forUser(UUID userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet.type = WalletType.USER;
        wallet.currency = currency;
        wallet.status = WalletStatus.ACTIVE;
        return wallet;
    }

    public static Wallet system(String systemCode, String currency) {
        Wallet wallet = new Wallet();
        wallet.systemCode = systemCode;
        wallet.type = WalletType.SYSTEM;
        wallet.currency = currency;
        wallet.status = WalletStatus.ACTIVE;
        return wallet;
    }

    public boolean isActive() {
        return status == WalletStatus.ACTIVE;
    }
}

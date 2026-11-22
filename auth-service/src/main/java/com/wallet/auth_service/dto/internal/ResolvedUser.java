package com.wallet.auth_service.dto.internal;

import com.wallet.auth_service.entity.User;

import java.util.UUID;

public record ResolvedUser(UUID userId, String email) {
    public static ResolvedUser from(User user) {
        return new ResolvedUser(user.getId(), user.getEmail());
    }
}
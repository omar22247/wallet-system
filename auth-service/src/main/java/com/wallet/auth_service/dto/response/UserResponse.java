package com.wallet.auth_service.dto.response;

import com.wallet.auth_service.entity.User;

import java.util.UUID;

public record UserResponse(UUID id, String email, String fullName) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName());
    }
}

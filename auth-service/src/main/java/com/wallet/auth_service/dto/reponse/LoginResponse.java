package com.wallet.auth_service.dto.reponse;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}

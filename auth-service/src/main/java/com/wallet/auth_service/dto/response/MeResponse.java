package com.wallet.auth_service.dto.response;

import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

public record MeResponse(
        String userId,
        String email,
        List<String> roles,
        Instant expiresAt
) {
    public static MeResponse from(Jwt jwt) {
        return new MeResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsStringList("roles"),
                jwt.getExpiresAt()
        );
    }
}

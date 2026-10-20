package com.wallet.wallet_service.security;

import com.nimbusds.jwt.JWT;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.Locale;
import java.util.UUID;


public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Jwt jwt) {

        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidBearerTokenException("Token subject is not a valid user id");
        }
    }

}

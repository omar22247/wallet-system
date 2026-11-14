package com.wallet.auth_service.controller;

import com.wallet.auth_service.dto.request.UserLookupRequest;
import com.wallet.auth_service.dto.response.ApiResponse;
import com.wallet.auth_service.dto.response.UserLookupResponse;
import com.wallet.auth_service.service.UserLookupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserLookupService userLookupService;

    @PostMapping("/lookup")
    public ApiResponse<UserLookupResponse> lookup(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody UserLookupRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(userLookupService.lookupByEmail(requesterId, request.email()));
    }
}
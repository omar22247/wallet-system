package com.wallet.auth_service.controller;

import com.wallet.auth_service.dto.response.LoginResponse;
import com.wallet.auth_service.dto.response.MeResponse;
import com.wallet.auth_service.dto.response.MessageResponse;
import com.wallet.auth_service.dto.response.UserResponse;
import com.wallet.auth_service.dto.request.ChangePasswordRequest;
import com.wallet.auth_service.dto.request.LoginRequest;
import com.wallet.auth_service.dto.request.RegisterRequest;
import com.wallet.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@EnableMethodSecurity
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(UUID.fromString(jwt.getSubject()), request);
        return new MessageResponse("Password changed successfully.");
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return MeResponse.from(jwt);
    }
}

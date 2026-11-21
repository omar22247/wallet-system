package com.wallet.auth_service.controller;

import com.wallet.auth_service.dto.request.*;
import com.wallet.auth_service.dto.response.ApiResponse;
import com.wallet.auth_service.dto.response.LoginResponse;
import com.wallet.auth_service.dto.response.MeResponse;
import com.wallet.auth_service.dto.response.UserResponse;
import com.wallet.auth_service.service.AuthService;
import com.wallet.auth_service.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse tokens = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", tokens));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(UUID.fromString(jwt.getSubject()), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.accepted()
                .body(ApiResponse.success("If the email is registered, a reset link has been sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset", null));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(MeResponse.from(jwt)));
    }
}
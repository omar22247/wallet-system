package com.wallet.auth_service.controller;

import com.wallet.auth_service.dto.request.RegisterCompleteRequest;
import com.wallet.auth_service.dto.request.RegisterStartRequest;
import com.wallet.auth_service.dto.response.ApiResponse;
import com.wallet.auth_service.dto.response.UserResponse;
import com.wallet.auth_service.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Void>> start(@Valid @RequestBody RegisterStartRequest request) {
        registrationService.start(request.email());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Verification code sent", null));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<UserResponse>> complete(
            @Valid @RequestBody RegisterCompleteRequest request) {
        UserResponse user = registrationService.complete(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created", user));
    }
}
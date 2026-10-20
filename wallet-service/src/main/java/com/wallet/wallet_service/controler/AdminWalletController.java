package com.wallet.wallet_service.controler;

import com.wallet.wallet_service.dto.request.TopUpRequest;
import com.wallet.wallet_service.dto.response.ApiResponse;
import com.wallet.wallet_service.dto.response.TopUpResponse;
import com.wallet.wallet_service.security.CurrentUser;
import com.wallet.wallet_service.service.TopUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/wallets")
public class AdminWalletController {

    private final TopUpService topUpService;



    @PostMapping("/top-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TopUpResponse>> topUp(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody TopUpRequest request) {
        TopUpResponse result = topUpService.topUp(CurrentUser.id(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Top-up completed", result));
    }
}
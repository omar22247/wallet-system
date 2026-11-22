package com.wallet.wallet_service.controler;

import com.wallet.wallet_service.dto.request.TopUpRequest;
import com.wallet.wallet_service.dto.response.ApiResponse;
import com.wallet.wallet_service.dto.response.PageResponse;
import com.wallet.wallet_service.dto.response.TopUpResponse;
import com.wallet.wallet_service.dto.response.TransactionResponse;
import com.wallet.wallet_service.dto.response.WalletResponse;
import com.wallet.wallet_service.security.CurrentUser;
import com.wallet.wallet_service.service.AdminWalletService;
import com.wallet.wallet_service.service.TopUpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/wallets")
public class AdminWalletController {

    private final TopUpService topUpService;
    private final AdminWalletService adminWalletService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> wallet(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(adminWalletService.getWallet(CurrentUser.id(jwt), userId)));
    }

    @PostMapping("/users/{userId}/freeze")
    public ResponseEntity<ApiResponse<Void>> freeze(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID userId) {
        adminWalletService.freeze(CurrentUser.id(jwt), userId);
        return ResponseEntity.ok(ApiResponse.success("Wallet frozen", null));
    }

    @PostMapping("/users/{userId}/unfreeze")
    public ResponseEntity<ApiResponse<Void>> unfreeze(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable UUID userId) {
        adminWalletService.unfreeze(CurrentUser.id(jwt), userId);
        return ResponseEntity.ok(ApiResponse.success("Wallet unfrozen", null));
    }

    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> transactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved",
                adminWalletService.getTransactions(CurrentUser.id(jwt), userId, page, size)));
    }

    @GetMapping("/users/{userId}/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> transaction(@AuthenticationPrincipal Jwt jwt,
                                                                        @PathVariable UUID userId,
                                                                        @PathVariable UUID transactionId) {
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved",
                adminWalletService.getTransaction(CurrentUser.id(jwt), userId, transactionId)));
    }

    @PostMapping("/top-up")
    public ResponseEntity<ApiResponse<TopUpResponse>> topUp(@AuthenticationPrincipal Jwt jwt,
                                                            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
                                                            @Valid @RequestBody TopUpRequest request) {
        TopUpResponse result = topUpService.topUp(CurrentUser.id(jwt), idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Top-up completed", result));
    }
}
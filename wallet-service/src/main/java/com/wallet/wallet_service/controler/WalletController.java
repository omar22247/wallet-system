package com.wallet.wallet_service.controler;

import com.wallet.wallet_service.dto.response.ApiResponse;
import com.wallet.wallet_service.dto.response.PageResponse;
import com.wallet.wallet_service.dto.response.TransactionResponse;
import com.wallet.wallet_service.dto.response.WalletResponse;
import com.wallet.wallet_service.entity.Wallet;
import com.wallet.wallet_service.repository.LedgerEntryRepository;
import com.wallet.wallet_service.repository.WalletRepository;
import com.wallet.wallet_service.security.CurrentUser;
import com.wallet.wallet_service.service.WalletService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> myWallet(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getMyWallet(CurrentUser.id(jwt))));
    }
    @GetMapping("/me/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> myTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        PageResponse<TransactionResponse> result =
                walletService.getMyTransactions(CurrentUser.id(jwt), page, size);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", result));
    }
    @GetMapping("/me/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> myTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        TransactionResponse result = walletService.getMyTransaction(CurrentUser.id(jwt), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved", result));
    }
}

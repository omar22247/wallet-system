package com.wallet.wallet_service.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

        @NotNull
        UUID toUserId,

        @NotNull
        @DecimalMin(value = "0.01")
        @DecimalMax(value = "100000.00")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @Size(max = 255)
        String description
) {}
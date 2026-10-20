package com.wallet.wallet_service.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;


public record TopUpRequest(

        @NotNull
        UUID userId,

        @NotNull
        @DecimalMin(value = "0.01", message = "must be at least 0.01")
        @DecimalMax(value = "100000.00", message = "must not exceed 100000.00")
        @Digits(integer = 17, fraction = 2, message = "must have at most 2 decimal places")
        BigDecimal amount,

        @Size(max = 255)
        String description
) {
}

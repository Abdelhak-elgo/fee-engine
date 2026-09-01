package com.elgourmat.fee_engine.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        String currency,

        @NotBlank(message = "customerType is required")
        String customerType,

        @NotBlank(message = "channel is required")
        String channel,

        @NotBlank(message = "countryCode is required")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "countryCode must be ISO-3166 alpha-2")
        String countryCode
) {
}

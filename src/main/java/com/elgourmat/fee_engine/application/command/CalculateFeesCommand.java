package com.elgourmat.fee_engine.application.command;

import java.math.BigDecimal;
import java.util.Objects;

public record CalculateFeesCommand(
        BigDecimal amount,
        String currency,
        String customerType,
        String channel,
        String countryCode
) {
    public CalculateFeesCommand {
        Objects.requireNonNull(amount, "amount required");
        Objects.requireNonNull(currency, "currency required");
        Objects.requireNonNull(customerType, "customerType required");
        Objects.requireNonNull(channel, "channel required");
        Objects.requireNonNull(countryCode, "countryCode required");
    }
}

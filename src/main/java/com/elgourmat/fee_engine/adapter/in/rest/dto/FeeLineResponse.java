package com.elgourmat.fee_engine.adapter.in.rest.dto;

import java.math.BigDecimal;

public record FeeLineResponse(
        String ruleName,
        String type,
        BigDecimal amount,
        String currency,
        String reason
) {
}

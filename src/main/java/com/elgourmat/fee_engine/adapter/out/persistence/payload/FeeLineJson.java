package com.elgourmat.fee_engine.adapter.out.persistence.payload;

import java.math.BigDecimal;

public record FeeLineJson(
        String ruleName,
        String type,
        BigDecimal amount,
        String currency,
        String reason
) {
}

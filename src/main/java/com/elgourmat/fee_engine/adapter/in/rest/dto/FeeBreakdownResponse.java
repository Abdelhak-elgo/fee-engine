package com.elgourmat.fee_engine.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;

public record FeeBreakdownResponse(
        String currency,
        BigDecimal transactionAmount,
        List<FeeLineResponse> lines,
        BigDecimal totalFees,
        BigDecimal grandTotal
) {
}

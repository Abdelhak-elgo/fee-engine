package com.elgourmat.fee_engine.application.model;

import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.Transaction;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record FeeCalculationRecord(
        UUID id,
        Instant calculatedAt,
        Transaction transaction,
        FeeBreakdown breakdown
) {
    public FeeCalculationRecord {
        Objects.requireNonNull(id, "id required");
        Objects.requireNonNull(calculatedAt, "calculatedAt required");
        Objects.requireNonNull(transaction, "transaction required");
        Objects.requireNonNull(breakdown, "breakdown required");
    }

    public static FeeCalculationRecord of(Transaction tx, FeeBreakdown breakdown, Instant now) {
        return new FeeCalculationRecord(UUID.randomUUID(), now, tx, breakdown);
    }
}

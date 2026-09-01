package com.elgourmat.fee_engine.domain.model;

import com.elgourmat.fee_engine.domain.exception.CurrencyMismatchException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record FeeBreakdown(Transaction transaction, List<FeeLine> lines) {

    public FeeBreakdown {
        Objects.requireNonNull(transaction, "La transaction est obligatoire");
        Objects.requireNonNull(lines, "Les lignes sont obligatoires");
        lines = List.copyOf(lines);
    }

    public static FeeBreakdown empty(Transaction transaction) {
        return new FeeBreakdown(transaction, List.of());
    }

    public FeeBreakdown withLine(FeeLine line) {
        Objects.requireNonNull(line, "La ligne est obligatoire");
        if (line.amount().currency() != transaction.currency()) {
            throw new CurrencyMismatchException(
                    "Devise de la ligne incompatible avec la transaction : %s vs %s"
                            .formatted(line.amount().currency(), transaction.currency()));
        }
        List<FeeLine> next = new ArrayList<>(lines);
        next.add(line);
        return new FeeBreakdown(transaction, next);
    }

    public Money totalFees() {
        BigDecimal sum = lines.stream()
                .map(FeeLine::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.signum() < 0) {
            return Money.zero(transaction.currency());
        }
        return Money.of(sum, transaction.currency());
    }

    public Money grandTotal() {
        return transaction.amount().add(totalFees());
    }
}

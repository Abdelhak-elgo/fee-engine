package com.elgourmat.fee_engine.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record FeeLine(String ruleName, FeeLineType type, Money amount, String reason) {

    public FeeLine {
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("Le nom de la règle est obligatoire");
        }
        Objects.requireNonNull(type, "Le type de ligne est obligatoire");
        Objects.requireNonNull(amount, "Le montant est obligatoire");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("La raison est obligatoire");
        }
    }

    public static FeeLine charge(String ruleName, Money amount, String reason) {
        return new FeeLine(ruleName, FeeLineType.CHARGE, amount, reason);
    }

    public static FeeLine discount(String ruleName, Money amount, String reason) {
        return new FeeLine(ruleName, FeeLineType.DISCOUNT, amount, reason);
    }

    public BigDecimal signedAmount() {
        return type == FeeLineType.CHARGE ? amount.amount() : amount.amount().negate();
    }
}

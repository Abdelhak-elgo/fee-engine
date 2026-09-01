package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.model.Transaction;

import java.util.Objects;

public record CapRule(String name, Money maxFees) implements FeeRule {

    public CapRule {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la règle est obligatoire");
        }
        Objects.requireNonNull(maxFees, "Le plafond est obligatoire");
    }

    @Override
    public boolean appliesTo(Transaction tx, FeeBreakdown current) {
        return tx.currency() == maxFees.currency()
                && current.totalFees().isGreaterThan(maxFees);
    }

    @Override
    public FeeLine apply(Transaction tx, FeeBreakdown current) {
        Money excess = current.totalFees().subtract(maxFees);
        String reason = "Plafond " + maxFees.amount() + " " + maxFees.currency() + " atteint";
        return FeeLine.discount(name, excess, reason);
    }
}

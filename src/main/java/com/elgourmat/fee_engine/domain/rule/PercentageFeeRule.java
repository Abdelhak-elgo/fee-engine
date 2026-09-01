package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.Objects;

public record PercentageFeeRule(String name, BigDecimal rate) implements FeeRule {

    public PercentageFeeRule {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la règle est obligatoire");
        }
        Objects.requireNonNull(rate, "Le taux est obligatoire");
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("Le taux ne peut pas être négatif : " + rate);
        }
    }

    @Override
    public boolean appliesTo(Transaction tx, FeeBreakdown current) {
        return true;
    }

    @Override
    public FeeLine apply(Transaction tx, FeeBreakdown current) {
        Money fee = tx.amount().multiply(rate);
        String reason = "Commission " + rate.movePointRight(2).stripTrailingZeros().toPlainString() + "%";
        return FeeLine.charge(name, fee, reason);
    }
}

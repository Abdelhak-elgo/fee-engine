package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.CustomerType;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Transaction;

import java.util.Objects;
import java.util.Set;

public record ExemptionRule(String name, Set<CustomerType> exemptedTypes) implements FeeRule {

    public ExemptionRule {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la règle est obligatoire");
        }
        Objects.requireNonNull(exemptedTypes, "Les types exemptés sont obligatoires");
        if (exemptedTypes.isEmpty()) {
            throw new IllegalArgumentException("Au moins un type client doit être exempté");
        }
        exemptedTypes = Set.copyOf(exemptedTypes);
    }

    @Override
    public boolean appliesTo(Transaction tx, FeeBreakdown current) {
        return exemptedTypes.contains(tx.customerType()) && !current.totalFees().isZero();
    }

    @Override
    public FeeLine apply(Transaction tx, FeeBreakdown current) {
        return FeeLine.discount(name, current.totalFees(), "Client " + tx.customerType() + " exempté");
    }
}

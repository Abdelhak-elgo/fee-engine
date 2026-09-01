package com.elgourmat.fee_engine.domain.service;

import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Transaction;
import com.elgourmat.fee_engine.domain.rule.FeeRule;

import java.util.List;
import java.util.Objects;

public final class FeeEngine {

    private final List<FeeRule> rules;

    public FeeEngine(List<FeeRule> rules) {
        Objects.requireNonNull(rules, "Les règles sont obligatoires");
        this.rules = List.copyOf(rules);
    }

    public FeeBreakdown calculate(Transaction tx) {
        Objects.requireNonNull(tx, "La transaction est obligatoire");
        FeeBreakdown breakdown = FeeBreakdown.empty(tx);
        for (FeeRule rule : rules) {
            if (rule.appliesTo(tx, breakdown)) {
                FeeLine line = rule.apply(tx, breakdown);
                breakdown = breakdown.withLine(line);
            }
        }
        return breakdown;
    }
}

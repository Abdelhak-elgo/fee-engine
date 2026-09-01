package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.Channel;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.model.Transaction;

import java.util.Objects;
import java.util.Set;

public record FixedFeeRule(String name, Money fixedAmount, Set<Channel> applicableChannels) implements FeeRule {

    public FixedFeeRule {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la règle est obligatoire");
        }
        Objects.requireNonNull(fixedAmount, "Le montant fixe est obligatoire");
        Objects.requireNonNull(applicableChannels, "Les canaux sont obligatoires");
        if (applicableChannels.isEmpty()) {
            throw new IllegalArgumentException("Au moins un canal doit être défini");
        }
        applicableChannels = Set.copyOf(applicableChannels);
    }

    @Override
    public boolean appliesTo(Transaction tx, FeeBreakdown current) {
        return applicableChannels.contains(tx.channel())
                && tx.currency() == fixedAmount.currency();
    }

    @Override
    public FeeLine apply(Transaction tx, FeeBreakdown current) {
        return FeeLine.charge(name, fixedAmount, "Frais fixes canal " + tx.channel());
    }
}

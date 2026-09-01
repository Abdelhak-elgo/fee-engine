package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Transaction;

public interface FeeRule {

    boolean appliesTo(Transaction tx, FeeBreakdown current);

    FeeLine apply(Transaction tx, FeeBreakdown current);

    String name();
}

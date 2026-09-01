package com.elgourmat.fee_engine.application.port.out;

import com.elgourmat.fee_engine.domain.rule.FeeRule;

import java.util.List;

public interface RuleProvider {
    List<FeeRule> load();
}

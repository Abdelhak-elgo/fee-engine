package com.elgourmat.fee_engine.adapter.out.rule;

import com.elgourmat.fee_engine.application.port.out.RuleProvider;
import com.elgourmat.fee_engine.domain.model.Channel;
import com.elgourmat.fee_engine.domain.model.Currency;
import com.elgourmat.fee_engine.domain.model.CustomerType;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.rule.CapRule;
import com.elgourmat.fee_engine.domain.rule.ExemptionRule;
import com.elgourmat.fee_engine.domain.rule.FeeRule;
import com.elgourmat.fee_engine.domain.rule.FixedFeeRule;
import com.elgourmat.fee_engine.domain.rule.PercentageFeeRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class InMemoryRuleProvider implements RuleProvider {

    @Override
    public List<FeeRule> load() {
        return List.of(
                new PercentageFeeRule("Commission 1.5%", new BigDecimal("0.015")),
                new FixedFeeRule("Frais fixes BRANCH",
                        Money.of(new BigDecimal("2.00"), Currency.MAD),
                        Set.of(Channel.BRANCH)),
                new ExemptionRule("Exemption CORPORATE",
                        Set.of(CustomerType.CORPORATE)),
                new CapRule("Plafond 100 MAD",
                        Money.of(new BigDecimal("100.00"), Currency.MAD))
        );
    }
}

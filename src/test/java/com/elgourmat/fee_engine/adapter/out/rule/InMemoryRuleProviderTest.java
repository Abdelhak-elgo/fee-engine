package com.elgourmat.fee_engine.adapter.out.rule;

import com.elgourmat.fee_engine.domain.rule.CapRule;
import com.elgourmat.fee_engine.domain.rule.ExemptionRule;
import com.elgourmat.fee_engine.domain.rule.FeeRule;
import com.elgourmat.fee_engine.domain.rule.FixedFeeRule;
import com.elgourmat.fee_engine.domain.rule.PercentageFeeRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRuleProviderTest {

    @Test
    void retourne_les_4_regles_par_defaut() {
        InMemoryRuleProvider provider = new InMemoryRuleProvider();

        List<FeeRule> rules = provider.load();

        assertEquals(4, rules.size());
    }

    @Test
    void respecte_l_ordre_percent_fixed_exemption_cap() {
        InMemoryRuleProvider provider = new InMemoryRuleProvider();

        List<FeeRule> rules = provider.load();

        assertInstanceOf(PercentageFeeRule.class, rules.get(0));
        assertInstanceOf(FixedFeeRule.class, rules.get(1));
        assertInstanceOf(ExemptionRule.class, rules.get(2));
        assertInstanceOf(CapRule.class, rules.get(3));
    }

    @Test
    void chaque_appel_retourne_les_memes_regles() {
        InMemoryRuleProvider provider = new InMemoryRuleProvider();

        assertEquals(provider.load().size(), provider.load().size());
    }
}

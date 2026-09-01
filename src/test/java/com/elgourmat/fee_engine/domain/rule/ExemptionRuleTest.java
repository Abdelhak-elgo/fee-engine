package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExemptionRuleTest {

    private static final Money CENT_MAD = Money.of(new BigDecimal("100.00"), Currency.MAD);
    private static final Money DIX_MAD = Money.of(new BigDecimal("10.00"), Currency.MAD);

    @Test
    void annule_les_frais_pour_client_exempte() {
        ExemptionRule rule = new ExemptionRule("Exemption CORPORATE", Set.of(CustomerType.CORPORATE));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.CORPORATE, Channel.ONLINE, "MA");
        FeeBreakdown current = FeeBreakdown.empty(tx).withLine(FeeLine.charge("X", DIX_MAD, "test"));
        assertTrue(rule.appliesTo(tx, current));
        FeeLine line = rule.apply(tx, current);
        assertEquals(FeeLineType.DISCOUNT, line.type());
        assertEquals(DIX_MAD, line.amount());
    }

    @Test
    void ignore_si_type_non_exempte() {
        ExemptionRule rule = new ExemptionRule("Exemption CORPORATE", Set.of(CustomerType.CORPORATE));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown current = FeeBreakdown.empty(tx).withLine(FeeLine.charge("X", DIX_MAD, "test"));
        assertFalse(rule.appliesTo(tx, current));
    }

    @Test
    void ignore_si_aucun_frais_courant() {
        ExemptionRule rule = new ExemptionRule("Exemption CORPORATE", Set.of(CustomerType.CORPORATE));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.CORPORATE, Channel.ONLINE, "MA");
        assertFalse(rule.appliesTo(tx, FeeBreakdown.empty(tx)));
    }

    @Test
    void refuse_ensemble_vide() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExemptionRule("X", Set.of()));
    }

    @Test
    void refuse_types_null() {
        assertThrows(NullPointerException.class,
                () -> new ExemptionRule("X", null));
    }
}

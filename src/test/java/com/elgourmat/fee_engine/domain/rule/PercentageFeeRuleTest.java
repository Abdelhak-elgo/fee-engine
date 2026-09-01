package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PercentageFeeRuleTest {

    private static final Money CENT_MAD = Money.of(new BigDecimal("100.00"), Currency.MAD);
    private static final Transaction TX = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
    private static final FeeBreakdown BREAKDOWN = FeeBreakdown.empty(TX);

    @Test
    void applique_le_taux_configure() {
        PercentageFeeRule rule = new PercentageFeeRule("Commission", new BigDecimal("0.015"));
        FeeLine line = rule.apply(TX, BREAKDOWN);
        assertEquals(Money.of(new BigDecimal("1.50"), Currency.MAD), line.amount());
        assertEquals(FeeLineType.CHARGE, line.type());
    }

    @Test
    void applique_zero_sur_transaction_zero() {
        Transaction txZero = new Transaction(Money.zero(Currency.MAD), CustomerType.STANDARD, Channel.ONLINE, "MA");
        PercentageFeeRule rule = new PercentageFeeRule("Commission", new BigDecimal("0.015"));
        FeeLine line = rule.apply(txZero, FeeBreakdown.empty(txZero));
        assertTrue(line.amount().isZero());
    }

    @Test
    void nomme_correctement_la_regle() {
        PercentageFeeRule rule = new PercentageFeeRule("Commission Standard", new BigDecimal("0.015"));
        assertEquals("Commission Standard", rule.name());
    }

    @Test
    void toujours_applicable() {
        PercentageFeeRule rule = new PercentageFeeRule("Commission", new BigDecimal("0.015"));
        assertTrue(rule.appliesTo(TX, BREAKDOWN));
    }

    @Test
    void refuse_taux_null() {
        assertThrows(NullPointerException.class, () -> new PercentageFeeRule("X", null));
    }

    @Test
    void refuse_taux_negatif() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageFeeRule("X", new BigDecimal("-0.01")));
    }

    @Test
    void refuse_nom_vide() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageFeeRule("", new BigDecimal("0.01")));
    }
}

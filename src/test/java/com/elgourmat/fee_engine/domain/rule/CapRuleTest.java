package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CapRuleTest {

    private static final Money CENT_MAD = Money.of(new BigDecimal("100.00"), Currency.MAD);
    private static final Money PLAFOND_50 = Money.of(new BigDecimal("50.00"), Currency.MAD);

    @Test
    void ignore_si_total_sous_plafond() {
        CapRule rule = new CapRule("Plafond", PLAFOND_50);
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown current = FeeBreakdown.empty(tx).withLine(
                FeeLine.charge("X", Money.of(new BigDecimal("30.00"), Currency.MAD), "test"));
        assertFalse(rule.appliesTo(tx, current));
    }

    @Test
    void ignore_si_total_egal_plafond() {
        CapRule rule = new CapRule("Plafond", PLAFOND_50);
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown current = FeeBreakdown.empty(tx).withLine(FeeLine.charge("X", PLAFOND_50, "test"));
        assertFalse(rule.appliesTo(tx, current));
    }

    @Test
    void plafonne_exactement_au_maximum() {
        CapRule rule = new CapRule("Plafond", PLAFOND_50);
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        Money soixanteDix = Money.of(new BigDecimal("70.00"), Currency.MAD);
        FeeBreakdown current = FeeBreakdown.empty(tx).withLine(FeeLine.charge("X", soixanteDix, "test"));
        assertTrue(rule.appliesTo(tx, current));
        FeeLine discount = rule.apply(tx, current);
        assertEquals(FeeLineType.DISCOUNT, discount.type());
        assertEquals(Money.of(new BigDecimal("20.00"), Currency.MAD), discount.amount());
        FeeBreakdown withDiscount = current.withLine(discount);
        assertEquals(PLAFOND_50, withDiscount.totalFees());
    }

    @Test
    void ignore_si_devise_differente() {
        Money plafondEur = Money.of(new BigDecimal("50.00"), Currency.EUR);
        CapRule rule = new CapRule("Plafond", plafondEur);
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown current = FeeBreakdown.empty(tx).withLine(
                FeeLine.charge("X", Money.of(new BigDecimal("100.00"), Currency.MAD), "test"));
        assertFalse(rule.appliesTo(tx, current));
    }

    @Test
    void refuse_plafond_null() {
        assertThrows(NullPointerException.class, () -> new CapRule("X", null));
    }

    @Test
    void refuse_nom_vide() {
        assertThrows(IllegalArgumentException.class, () -> new CapRule("", PLAFOND_50));
    }
}

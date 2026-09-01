package com.elgourmat.fee_engine.domain.service;

import com.elgourmat.fee_engine.domain.model.*;
import com.elgourmat.fee_engine.domain.rule.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FeeEngineTest {

    private static final Money CENT_MAD = Money.of(new BigDecimal("100.00"), Currency.MAD);

    @Test
    void moteur_sans_regle_retourne_breakdown_vide() {
        FeeEngine engine = new FeeEngine(List.of());
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown result = engine.calculate(tx);
        assertTrue(result.totalFees().isZero());
        assertEquals(0, result.lines().size());
    }

    @Test
    void moteur_avec_une_regle_applique_cette_regle() {
        FeeEngine engine = new FeeEngine(List.of(
                new PercentageFeeRule("Commission", new BigDecimal("0.015"))));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown result = engine.calculate(tx);
        assertEquals(Money.of(new BigDecimal("1.50"), Currency.MAD), result.totalFees());
    }

    @Test
    void moteur_respecte_ordre_des_regles() {
        FeeEngine engine = new FeeEngine(List.of(
                new PercentageFeeRule("Commission", new BigDecimal("0.015")),
                new FixedFeeRule("Frais canal",
                        Money.of(new BigDecimal("2.00"), Currency.MAD),
                        Set.of(Channel.ONLINE))));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown result = engine.calculate(tx);
        assertEquals(Money.of(new BigDecimal("3.50"), Currency.MAD), result.totalFees());
        assertEquals(2, result.lines().size());
        assertEquals("Commission", result.lines().get(0).ruleName());
        assertEquals("Frais canal", result.lines().get(1).ruleName());
    }

    @Test
    void moteur_gere_client_exempte() {
        FeeEngine engine = new FeeEngine(List.of(
                new PercentageFeeRule("Commission", new BigDecimal("0.015")),
                new ExemptionRule("Exemption CORPORATE", Set.of(CustomerType.CORPORATE))));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.CORPORATE, Channel.ONLINE, "MA");
        FeeBreakdown result = engine.calculate(tx);
        assertTrue(result.totalFees().isZero());
    }

    @Test
    void moteur_applique_cap_apres_calcul() {
        FeeEngine engine = new FeeEngine(List.of(
                new PercentageFeeRule("Commission", new BigDecimal("0.10")),
                new FixedFeeRule("Frais canal",
                        Money.of(new BigDecimal("15.00"), Currency.MAD),
                        Set.of(Channel.ONLINE)),
                new CapRule("Plafond", Money.of(new BigDecimal("20.00"), Currency.MAD))));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown result = engine.calculate(tx);
        assertEquals(Money.of(new BigDecimal("20.00"), Currency.MAD), result.totalFees());
    }

    @Test
    void moteur_est_immuable_par_rapport_a_la_liste_source() {
        List<FeeRule> mutable = new ArrayList<>();
        mutable.add(new PercentageFeeRule("Commission", new BigDecimal("0.015")));
        FeeEngine engine = new FeeEngine(mutable);
        mutable.clear();
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown result = engine.calculate(tx);
        assertEquals(Money.of(new BigDecimal("1.50"), Currency.MAD), result.totalFees());
    }

    @Test
    void refuse_transaction_null() {
        FeeEngine engine = new FeeEngine(List.of());
        assertThrows(NullPointerException.class, () -> engine.calculate(null));
    }
}

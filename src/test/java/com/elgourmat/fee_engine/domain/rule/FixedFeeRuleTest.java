package com.elgourmat.fee_engine.domain.rule;

import com.elgourmat.fee_engine.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FixedFeeRuleTest {

    private static final Money DEUX_MAD = Money.of(new BigDecimal("2.00"), Currency.MAD);
    private static final Money CENT_MAD = Money.of(new BigDecimal("100.00"), Currency.MAD);

    @Test
    void applique_le_montant_fixe_sur_canal_configure() {
        FixedFeeRule rule = new FixedFeeRule("Frais fixes", DEUX_MAD, Set.of(Channel.BRANCH));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.BRANCH, "MA");
        assertTrue(rule.appliesTo(tx, FeeBreakdown.empty(tx)));
        FeeLine line = rule.apply(tx, FeeBreakdown.empty(tx));
        assertEquals(DEUX_MAD, line.amount());
        assertEquals(FeeLineType.CHARGE, line.type());
    }

    @Test
    void ignore_si_canal_hors_ensemble() {
        FixedFeeRule rule = new FixedFeeRule("Frais fixes", DEUX_MAD, Set.of(Channel.BRANCH));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        assertFalse(rule.appliesTo(tx, FeeBreakdown.empty(tx)));
    }

    @Test
    void ignore_si_devise_differente() {
        Money deuxEur = Money.of(new BigDecimal("2.00"), Currency.EUR);
        FixedFeeRule rule = new FixedFeeRule("Frais fixes", deuxEur, Set.of(Channel.BRANCH));
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.BRANCH, "MA");
        assertFalse(rule.appliesTo(tx, FeeBreakdown.empty(tx)));
    }

    @Test
    void refuse_ensemble_vide() {
        assertThrows(IllegalArgumentException.class,
                () -> new FixedFeeRule("X", DEUX_MAD, Set.of()));
    }

    @Test
    void refuse_montant_null() {
        assertThrows(NullPointerException.class,
                () -> new FixedFeeRule("X", null, Set.of(Channel.BRANCH)));
    }

    @Test
    void copie_defensivement_le_set_de_canaux() {
        Set<Channel> mutable = new HashSet<>(Set.of(Channel.BRANCH));
        FixedFeeRule rule = new FixedFeeRule("Frais fixes", DEUX_MAD, mutable);
        mutable.add(Channel.ONLINE);
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");
        assertFalse(rule.appliesTo(tx, FeeBreakdown.empty(tx)));
    }
}

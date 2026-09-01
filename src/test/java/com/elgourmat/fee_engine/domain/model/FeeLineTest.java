package com.elgourmat.fee_engine.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FeeLineTest {

    private static final Money DIX_MAD = Money.of(new BigDecimal("10.00"), Currency.MAD);

    @Test
    void cree_une_ligne_charge_valide() {
        FeeLine line = FeeLine.charge("Commission", DIX_MAD, "1.5%");

        assertEquals("Commission", line.ruleName());
        assertEquals(FeeLineType.CHARGE, line.type());
        assertEquals(DIX_MAD, line.amount());
        assertEquals("1.5%", line.reason());
    }

    @Test
    void cree_une_ligne_remise_valide() {
        FeeLine line = FeeLine.discount("Cap", DIX_MAD, "Plafond atteint");

        assertEquals(FeeLineType.DISCOUNT, line.type());
    }

    @Test
    void refuse_un_nom_null() {
        assertThrows(IllegalArgumentException.class,
                () -> FeeLine.charge(null, DIX_MAD, "raison"));
    }

    @Test
    void refuse_un_nom_vide() {
        assertThrows(IllegalArgumentException.class,
                () -> FeeLine.charge("   ", DIX_MAD, "raison"));
    }

    @Test
    void refuse_un_montant_null() {
        assertThrows(NullPointerException.class,
                () -> FeeLine.charge("Nom", null, "raison"));
    }

    @Test
    void refuse_une_raison_vide() {
        assertThrows(IllegalArgumentException.class,
                () -> FeeLine.charge("Nom", DIX_MAD, ""));
    }

    @Test
    void signed_amount_positif_pour_charge() {
        FeeLine line = FeeLine.charge("Commission", DIX_MAD, "1.5%");

        assertEquals(new BigDecimal("10.00"), line.signedAmount());
    }

    @Test
    void signed_amount_negatif_pour_discount() {
        FeeLine line = FeeLine.discount("Cap", DIX_MAD, "Plafond");

        assertEquals(new BigDecimal("-10.00"), line.signedAmount());
    }
}

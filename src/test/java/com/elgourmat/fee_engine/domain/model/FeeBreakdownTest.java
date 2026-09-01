package com.elgourmat.fee_engine.domain.model;

import com.elgourmat.fee_engine.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FeeBreakdownTest {

    private static final Money CENT_MAD = Money.of(new BigDecimal("100.00"), Currency.MAD);
    private static final Money DIX_MAD = Money.of(new BigDecimal("10.00"), Currency.MAD);
    private static final Transaction TX = new Transaction(
            CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");

    @Test
    void empty_donne_total_frais_zero() {
        FeeBreakdown breakdown = FeeBreakdown.empty(TX);

        assertTrue(breakdown.totalFees().isZero());
    }

    @Test
    void empty_conserve_le_montant_transaction_en_grand_total() {
        FeeBreakdown breakdown = FeeBreakdown.empty(TX);

        assertEquals(CENT_MAD, breakdown.grandTotal());
    }

    @Test
    void with_line_ajoute_une_charge_au_total() {
        FeeBreakdown breakdown = FeeBreakdown.empty(TX)
                .withLine(FeeLine.charge("Commission", DIX_MAD, "1.5%"));

        assertEquals(DIX_MAD, breakdown.totalFees());
    }

    @Test
    void with_line_avec_discount_soustrait_du_total() {
        Money trois = Money.of(new BigDecimal("3.00"), Currency.MAD);
        FeeBreakdown breakdown = FeeBreakdown.empty(TX)
                .withLine(FeeLine.charge("Commission", DIX_MAD, "1.5%"))
                .withLine(FeeLine.discount("Loyalty", trois, "Remise fidélité"));

        assertEquals(Money.of(new BigDecimal("7.00"), Currency.MAD), breakdown.totalFees());
    }

    @Test
    void total_frais_est_clamp_a_zero_si_remises_superieures() {
        Money mille = Money.of(new BigDecimal("1000.00"), Currency.MAD);
        FeeBreakdown breakdown = FeeBreakdown.empty(TX)
                .withLine(FeeLine.charge("Commission", DIX_MAD, "1.5%"))
                .withLine(FeeLine.discount("Exemption", mille, "Client exempté"));

        assertTrue(breakdown.totalFees().isZero());
    }

    @Test
    void with_line_ne_mute_pas_l_original() {
        FeeBreakdown empty = FeeBreakdown.empty(TX);
        empty.withLine(FeeLine.charge("Commission", DIX_MAD, "1.5%"));

        assertEquals(0, empty.lines().size());
    }

    @Test
    void grand_total_egale_montant_transaction_plus_frais() {
        FeeBreakdown breakdown = FeeBreakdown.empty(TX)
                .withLine(FeeLine.charge("Commission", DIX_MAD, "1.5%"));

        assertEquals(Money.of(new BigDecimal("110.00"), Currency.MAD), breakdown.grandTotal());
    }

    @Test
    void refuse_une_ligne_de_devise_differente() {
        Money dixEur = Money.of(new BigDecimal("10.00"), Currency.EUR);
        FeeBreakdown breakdown = FeeBreakdown.empty(TX);

        assertThrows(CurrencyMismatchException.class,
                () -> breakdown.withLine(FeeLine.charge("Commission", dixEur, "1.5%")));
    }

    @Test
    void la_liste_des_lignes_est_immuable() {
        FeeBreakdown breakdown = FeeBreakdown.empty(TX)
                .withLine(FeeLine.charge("Commission", DIX_MAD, "1.5%"));

        assertThrows(UnsupportedOperationException.class,
                () -> breakdown.lines().add(FeeLine.charge("X", DIX_MAD, "y")));
    }
}

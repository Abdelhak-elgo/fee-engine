package com.elgourmat.fee_engine.domain.model;

import com.elgourmat.fee_engine.domain.exception.CurrencyMismatchException;
import com.elgourmat.fee_engine.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void normalise_l_echelle_selon_la_devise() {
        Money money = Money.of(new BigDecimal("10.5"), Currency.MAD);
        assertEquals(new BigDecimal("10.50"), money.amount());
    }

    @Test
    void arrondit_au_plus_proche_en_cas_de_decimales_excedentaires() {
        Money money = Money.of(new BigDecimal("10.567"), Currency.EUR);
        assertEquals(new BigDecimal("10.57"), money.amount());
    }

    @Test
    void refuse_un_montant_negatif() {
        assertThrows(InvalidAmountException.class,
                () -> Money.of(new BigDecimal("-1"), Currency.MAD));
    }

    @Test
    void refuse_un_montant_null() {
        assertThrows(InvalidAmountException.class,
                () -> Money.of(null, Currency.MAD));
    }

    @Test
    void additionne_deux_montants_de_meme_devise() {
        Money result = Money.of(new BigDecimal("10.00"), Currency.MAD)
                .add(Money.of(new BigDecimal("5.50"), Currency.MAD));
        assertEquals(Money.of(new BigDecimal("15.50"), Currency.MAD), result);
    }

    @Test
    void refuse_l_addition_de_devises_differentes() {
        Money mad = Money.of(new BigDecimal("10.00"), Currency.MAD);
        Money eur = Money.of(new BigDecimal("10.00"), Currency.EUR);
        assertThrows(CurrencyMismatchException.class, () -> mad.add(eur));
    }

    @Test
    void multiplie_par_un_taux_et_arrondit() {
        Money result = Money.of(new BigDecimal("100.00"), Currency.MAD)
                .multiply(new BigDecimal("0.015"));
        assertEquals(Money.of(new BigDecimal("1.50"), Currency.MAD), result);
    }

    @Test
    void retourne_le_plus_petit_des_deux_montants() {
        Money petit = Money.of(new BigDecimal("5.00"), Currency.MAD);
        Money grand = Money.of(new BigDecimal("10.00"), Currency.MAD);
        assertEquals(petit, petit.min(grand));
        assertEquals(petit, grand.min(petit));
    }

    @Test
    void compare_deux_montants() {
        Money grand = Money.of(new BigDecimal("10.00"), Currency.MAD);
        Money petit = Money.of(new BigDecimal("5.00"), Currency.MAD);
        assertTrue(grand.isGreaterThan(petit));
        assertFalse(petit.isGreaterThan(grand));
    }

    @Test
    void deux_montants_identiques_sont_egaux() {
        assertEquals(
                Money.of(new BigDecimal("10.5"), Currency.MAD),
                Money.of(new BigDecimal("10.50"), Currency.MAD));
    }
}
package com.elgourmat.fee_engine.domain.model;

import com.elgourmat.fee_engine.domain.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final Money CENT_MAD = Money.of(new BigDecimal("100.00"), Currency.MAD);

    @Test
    void cree_une_transaction_valide() {
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MA");

        assertEquals(CENT_MAD, tx.amount());
        assertEquals(CustomerType.STANDARD, tx.customerType());
        assertEquals(Channel.ONLINE, tx.channel());
        assertEquals("MA", tx.countryCode());
    }

    @Test
    void normalise_le_code_pays_en_majuscules() {
        Transaction tx = new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "ma");

        assertEquals("MA", tx.countryCode());
    }

    @Test
    void refuse_un_montant_null() {
        assertThrows(InvalidTransactionException.class,
                () -> new Transaction(null, CustomerType.STANDARD, Channel.ONLINE, "MA"));
    }

    @Test
    void refuse_un_type_client_null() {
        assertThrows(InvalidTransactionException.class,
                () -> new Transaction(CENT_MAD, null, Channel.ONLINE, "MA"));
    }

    @Test
    void refuse_un_canal_null() {
        assertThrows(InvalidTransactionException.class,
                () -> new Transaction(CENT_MAD, CustomerType.STANDARD, null, "MA"));
    }

    @Test
    void refuse_un_code_pays_null() {
        assertThrows(InvalidTransactionException.class,
                () -> new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, null));
    }

    @Test
    void refuse_un_code_pays_de_longueur_invalide() {
        assertThrows(InvalidTransactionException.class,
                () -> new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "MAR"));
    }

    @Test
    void refuse_un_code_pays_non_alphabetique() {
        assertThrows(InvalidTransactionException.class,
                () -> new Transaction(CENT_MAD, CustomerType.STANDARD, Channel.ONLINE, "M1"));
    }
}
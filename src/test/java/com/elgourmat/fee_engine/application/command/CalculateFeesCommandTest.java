package com.elgourmat.fee_engine.application.command;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculateFeesCommandTest {

    private static final BigDecimal CENT = new BigDecimal("100.00");

    @Test
    void accepte_command_valide() {
        CalculateFeesCommand cmd = new CalculateFeesCommand(CENT, "MAD", "STANDARD", "ONLINE", "MA");

        assertEquals(CENT, cmd.amount());
        assertEquals("MAD", cmd.currency());
        assertEquals("STANDARD", cmd.customerType());
        assertEquals("ONLINE", cmd.channel());
        assertEquals("MA", cmd.countryCode());
    }

    @Test
    void refuse_amount_null() {
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesCommand(null, "MAD", "STANDARD", "ONLINE", "MA"));
    }

    @Test
    void refuse_currency_null() {
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesCommand(CENT, null, "STANDARD", "ONLINE", "MA"));
    }

    @Test
    void refuse_customer_type_null() {
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesCommand(CENT, "MAD", null, "ONLINE", "MA"));
    }

    @Test
    void refuse_channel_null() {
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesCommand(CENT, "MAD", "STANDARD", null, "MA"));
    }

    @Test
    void refuse_country_code_null() {
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesCommand(CENT, "MAD", "STANDARD", "ONLINE", null));
    }
}

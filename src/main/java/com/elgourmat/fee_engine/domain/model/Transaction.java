package com.elgourmat.fee_engine.domain.model;

import com.elgourmat.fee_engine.domain.exception.InvalidTransactionException;
import java.util.regex.Pattern;

public record Transaction(
    Money amount,
    CustomerType customerType,
    Channel channel,
    String countryCode
) {
    private static final Pattern ISO_COUNTRY_CODE = Pattern.compile("^[A-Z]{2}$");

    public Transaction {
        if (amount == null) {
            throw new InvalidTransactionException("Le montant est obligatoire");
        }
        if (customerType == null) {
            throw new InvalidTransactionException("Le type de client est obligatoire");
        }
        if (channel == null) {
            throw new InvalidTransactionException("Le canal est obligatoire");
        }
        if (countryCode == null) {
            throw new InvalidTransactionException("Le code pays est obligatoire");
        }

        countryCode = countryCode.trim().toUpperCase();

        if (!ISO_COUNTRY_CODE.matcher(countryCode).matches()) {
            throw new InvalidTransactionException(
                    "Code pays ISO-3166 alpha-2 attendu, reçu : " + countryCode);
        }
    }

    public Currency currency() {
        return amount.currency();
    }
}

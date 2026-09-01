package com.elgourmat.fee_engine.domain.model;

import com.elgourmat.fee_engine.domain.exception.CurrencyMismatchException;
import com.elgourmat.fee_engine.domain.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) {
            throw new InvalidAmountException("Le montant ne peut pas être null");
        }
        if (currency == null) {
            throw new InvalidAmountException("La devise ne peut pas être null");
        }
        if (amount.signum() < 0) {
            throw new InvalidAmountException("Le montant ne peut pas être négatif : " + amount);
        }
        amount = amount.setScale(currency.decimalPlaces(), RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        if (other.isGreaterThan(this)) {
            throw new InvalidAmountException(
                    "Résultat négatif interdit : %s - %s".formatted(this.amount, other.amount));
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public Money min(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0 ? this : other;
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "L'opérande ne peut pas être null");
        if (this.currency != other.currency) {
            throw new CurrencyMismatchException(
                    "Devises incompatibles : %s et %s".formatted(this.currency, other.currency));
        }
    }
}
package com.elgourmat.fee_engine.domain.model;

public enum Currency {
    MAD(2),EUR(2),USD(2);
    private final int decimalPlaces;

    Currency(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public int decimalPlaces() {
        return decimalPlaces;
    }
}

package com.elgourmat.fee_engine.adapter.out.persistence.payload;

public class UnknownPayloadVersionException extends RuntimeException {
    public UnknownPayloadVersionException(int version) {
        super("Unsupported fee_calculation payload version: " + version);
    }
}

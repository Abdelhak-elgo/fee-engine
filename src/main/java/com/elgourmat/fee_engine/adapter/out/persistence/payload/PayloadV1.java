package com.elgourmat.fee_engine.adapter.out.persistence.payload;

import java.util.List;
import java.util.Objects;

public record PayloadV1(int version, List<FeeLineJson> lines) {

    public static final int CURRENT_VERSION = 1;

    public PayloadV1 {
        if (version != CURRENT_VERSION) {
            throw new UnknownPayloadVersionException(version);
        }
        Objects.requireNonNull(lines, "lines required");
        lines = List.copyOf(lines);
    }

    public static PayloadV1 of(List<FeeLineJson> lines) {
        return new PayloadV1(CURRENT_VERSION, lines);
    }
}

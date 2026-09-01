package com.elgourmat.fee_engine.adapter.out.persistence.mapper;

import com.elgourmat.fee_engine.adapter.out.persistence.jpa.FeeCalculationEntity;
import com.elgourmat.fee_engine.adapter.out.persistence.payload.FeeLineJson;
import com.elgourmat.fee_engine.adapter.out.persistence.payload.PayloadV1;
import com.elgourmat.fee_engine.application.model.FeeCalculationRecord;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Transaction;

import java.util.List;

public final class FeeCalculationEntityMapper {

    private FeeCalculationEntityMapper() {
    }

    public static FeeCalculationEntity fromRecord(FeeCalculationRecord record) {
        Transaction tx = record.transaction();
        FeeBreakdown breakdown = record.breakdown();

        List<FeeLineJson> lines = breakdown.lines().stream()
                .map(FeeCalculationEntityMapper::toJson)
                .toList();
        PayloadV1 payload = PayloadV1.of(lines);

        return new FeeCalculationEntity(
                record.id(),
                tx.amount().amount(),
                tx.currency().name(),
                tx.customerType().name(),
                tx.channel().name(),
                tx.countryCode(),
                breakdown.totalFees().amount(),
                breakdown.grandTotal().amount(),
                (short) PayloadV1.CURRENT_VERSION,
                payload,
                record.calculatedAt()
        );
    }

    private static FeeLineJson toJson(FeeLine line) {
        return new FeeLineJson(
                line.ruleName(),
                line.type().name(),
                line.amount().amount(),
                line.amount().currency().name(),
                line.reason()
        );
    }
}

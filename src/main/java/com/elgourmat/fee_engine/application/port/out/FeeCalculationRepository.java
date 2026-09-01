package com.elgourmat.fee_engine.application.port.out;

import com.elgourmat.fee_engine.application.model.FeeCalculationRecord;

public interface FeeCalculationRepository {
    void save(FeeCalculationRecord record);
}

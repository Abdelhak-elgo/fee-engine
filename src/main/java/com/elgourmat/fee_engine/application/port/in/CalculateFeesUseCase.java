package com.elgourmat.fee_engine.application.port.in;

import com.elgourmat.fee_engine.application.command.CalculateFeesCommand;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;

public interface CalculateFeesUseCase {
    FeeBreakdown handle(CalculateFeesCommand command);
}

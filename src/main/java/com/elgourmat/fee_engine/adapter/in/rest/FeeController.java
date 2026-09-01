package com.elgourmat.fee_engine.adapter.in.rest;

import com.elgourmat.fee_engine.adapter.in.rest.dto.FeeBreakdownResponse;
import com.elgourmat.fee_engine.adapter.in.rest.dto.TransactionRequest;
import com.elgourmat.fee_engine.adapter.in.rest.mapper.FeeRestMapper;
import com.elgourmat.fee_engine.application.port.in.CalculateFeesUseCase;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fees")
public class FeeController {

    private final CalculateFeesUseCase calculateFeesUseCase;
    private final FeeRestMapper mapper;

    public FeeController(CalculateFeesUseCase calculateFeesUseCase, FeeRestMapper mapper) {
        this.calculateFeesUseCase = calculateFeesUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/calculate")
    public ResponseEntity<FeeBreakdownResponse> calculate(@Valid @RequestBody TransactionRequest request) {
        FeeBreakdown breakdown = calculateFeesUseCase.handle(mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(breakdown));
    }
}

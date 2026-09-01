package com.elgourmat.fee_engine.adapter.out.persistence;

import com.elgourmat.fee_engine.adapter.out.persistence.jpa.FeeCalculationJpaRepository;
import com.elgourmat.fee_engine.adapter.out.persistence.mapper.FeeCalculationEntityMapper;
import com.elgourmat.fee_engine.application.model.FeeCalculationRecord;
import com.elgourmat.fee_engine.application.port.out.FeeCalculationRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FeeCalculationPersistenceAdapter implements FeeCalculationRepository {

    private final FeeCalculationJpaRepository jpaRepository;

    public FeeCalculationPersistenceAdapter(FeeCalculationJpaRepository jpaRepository) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository required");
    }

    @Override
    public void save(FeeCalculationRecord record) {
        jpaRepository.save(FeeCalculationEntityMapper.fromRecord(record));
    }
}

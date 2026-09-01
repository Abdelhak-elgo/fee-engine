package com.elgourmat.fee_engine.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeeCalculationJpaRepository extends JpaRepository<FeeCalculationEntity, UUID> {
}

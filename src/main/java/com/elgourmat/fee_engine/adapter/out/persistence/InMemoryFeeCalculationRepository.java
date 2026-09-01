package com.elgourmat.fee_engine.adapter.out.persistence;

import com.elgourmat.fee_engine.application.model.FeeCalculationRecord;
import com.elgourmat.fee_engine.application.port.out.FeeCalculationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Placeholder Phase 3 — sera remplacé par l'adapter JPA en Phase 5.
 * Ne pas utiliser en production : les données sont perdues au redémarrage.
 */
@Component
public class InMemoryFeeCalculationRepository implements FeeCalculationRepository {

    private final List<FeeCalculationRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public void save(FeeCalculationRecord record) {
        records.add(record);
    }

    public List<FeeCalculationRecord> all() {
        return List.copyOf(records);
    }
}

package com.elgourmat.fee_engine.adapter.out.persistence;

import com.elgourmat.fee_engine.adapter.out.persistence.jpa.FeeCalculationEntity;
import com.elgourmat.fee_engine.adapter.out.persistence.jpa.FeeCalculationJpaRepository;
import com.elgourmat.fee_engine.adapter.out.persistence.payload.PayloadV1;
import com.elgourmat.fee_engine.application.model.FeeCalculationRecord;
import com.elgourmat.fee_engine.domain.model.Channel;
import com.elgourmat.fee_engine.domain.model.Currency;
import com.elgourmat.fee_engine.domain.model.CustomerType;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FeeCalculationPersistenceAdapterTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeeCalculationPersistenceAdapter adapter;

    @Autowired
    private FeeCalculationJpaRepository jpaRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("la migration Flyway crée bien la table fee_calculation")
    void migration_flyway_appliquée() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'fee_calculation'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("sauvegarde puis lecture d'un payload JSON structuré")
    void sauvegarde_puis_lecture_payload_json_structuré() {
        FeeCalculationRecord record = sampleRecord();
        adapter.save(record);

        FeeCalculationEntity read = jpaRepository.findById(record.id()).orElseThrow();

        assertThat(read.getTransactionAmount()).isEqualByComparingTo("100.00");
        assertThat(read.getTransactionCurrency()).isEqualTo("MAD");
        assertThat(read.getCustomerType()).isEqualTo("STANDARD");
        assertThat(read.getChannel()).isEqualTo("ONLINE");
        assertThat(read.getCountryCode()).isEqualTo("MA");
        assertThat(read.getTotalFees()).isEqualByComparingTo("1.50");
        assertThat(read.getGrandTotal()).isEqualByComparingTo("101.50");
        assertThat(read.getPayloadVersion()).isEqualTo((short) 1);

        PayloadV1 payload = read.getPayload();
        assertThat(payload.version()).isEqualTo(1);
        assertThat(payload.lines()).hasSize(1);
        assertThat(payload.lines().get(0).ruleName()).isEqualTo("Commission 1.5%");
        assertThat(payload.lines().get(0).type()).isEqualTo("CHARGE");
        assertThat(payload.lines().get(0).amount()).isEqualByComparingTo("1.50");
        assertThat(payload.lines().get(0).currency()).isEqualTo("MAD");
    }

    @Test
    @DisplayName("le payload persisté est bien un JSONB natif Postgres")
    void version_1_dé_sérialise_OK() {
        FeeCalculationRecord record = sampleRecord();
        adapter.save(record);

        String jsonType = jdbc.queryForObject(
                "SELECT pg_typeof(payload)::text FROM fee_calculation WHERE id = ?",
                String.class, record.id());
        assertThat(jsonType).isEqualTo("jsonb");

        String ruleName = jdbc.queryForObject(
                "SELECT payload -> 'lines' -> 0 ->> 'ruleName' FROM fee_calculation WHERE id = ?",
                String.class, record.id());
        assertThat(ruleName).isEqualTo("Commission 1.5%");
    }

    private FeeCalculationRecord sampleRecord() {
        Transaction tx = new Transaction(
                Money.of(new BigDecimal("100.00"), Currency.MAD),
                CustomerType.STANDARD,
                Channel.ONLINE,
                "MA");
        FeeBreakdown breakdown = FeeBreakdown.empty(tx)
                .withLine(FeeLine.charge("Commission 1.5%",
                        Money.of(new BigDecimal("1.50"), Currency.MAD),
                        "Commission proportionnelle"));
        return new FeeCalculationRecord(UUID.randomUUID(), Instant.now(), tx, breakdown);
    }
}

package com.elgourmat.fee_engine.adapter.in.rest.mapper;

import com.elgourmat.fee_engine.adapter.in.rest.dto.FeeBreakdownResponse;
import com.elgourmat.fee_engine.adapter.in.rest.dto.FeeLineResponse;
import com.elgourmat.fee_engine.adapter.in.rest.dto.TransactionRequest;
import com.elgourmat.fee_engine.application.command.CalculateFeesCommand;
import com.elgourmat.fee_engine.domain.model.Channel;
import com.elgourmat.fee_engine.domain.model.Currency;
import com.elgourmat.fee_engine.domain.model.CustomerType;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeeRestMapperTest {

    private final FeeRestMapper mapper = new FeeRestMapperImpl();

    @Test
    @DisplayName("normalise currency, customerType et channel en majuscules")
    void normalise_currency_en_majuscules() {
        TransactionRequest req = new TransactionRequest(
                new BigDecimal("100.00"), "mad", "standard", "online", "ma");

        CalculateFeesCommand cmd = mapper.toCommand(req);

        assertThat(cmd.currency()).isEqualTo("MAD");
        assertThat(cmd.customerType()).isEqualTo("STANDARD");
        assertThat(cmd.channel()).isEqualTo("ONLINE");
        assertThat(cmd.countryCode()).isEqualTo("MA");
        assertThat(cmd.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("mappe un breakdown complet avec ses lignes")
    void mappe_breakdown_complet_avec_lignes() {
        Transaction tx = new Transaction(
                Money.of(new BigDecimal("200.00"), Currency.MAD),
                CustomerType.STANDARD,
                Channel.BRANCH,
                "MA");
        FeeBreakdown breakdown = FeeBreakdown.empty(tx)
                .withLine(FeeLine.charge("Commission 1.5%",
                        Money.of(new BigDecimal("3.00"), Currency.MAD),
                        "Commission proportionnelle"))
                .withLine(FeeLine.charge("Frais fixes BRANCH",
                        Money.of(new BigDecimal("2.00"), Currency.MAD),
                        "Frais canal BRANCH"));

        FeeBreakdownResponse response = mapper.toResponse(breakdown);

        assertThat(response.currency()).isEqualTo("MAD");
        assertThat(response.transactionAmount()).isEqualByComparingTo("200.00");
        assertThat(response.totalFees()).isEqualByComparingTo("5.00");
        assertThat(response.grandTotal()).isEqualByComparingTo("205.00");
        assertThat(response.lines()).hasSize(2);

        FeeLineResponse first = response.lines().get(0);
        assertThat(first.ruleName()).isEqualTo("Commission 1.5%");
        assertThat(first.type()).isEqualTo("CHARGE");
        assertThat(first.amount()).isEqualByComparingTo("3.00");
        assertThat(first.currency()).isEqualTo("MAD");
        assertThat(first.reason()).isEqualTo("Commission proportionnelle");
    }
}

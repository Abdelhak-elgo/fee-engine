package com.elgourmat.fee_engine.application.service;

import com.elgourmat.fee_engine.application.command.CalculateFeesCommand;
import com.elgourmat.fee_engine.application.model.FeeCalculationRecord;
import com.elgourmat.fee_engine.application.port.out.FeeCalculationRepository;
import com.elgourmat.fee_engine.application.port.out.RuleProvider;
import com.elgourmat.fee_engine.domain.model.Channel;
import com.elgourmat.fee_engine.domain.model.Currency;
import com.elgourmat.fee_engine.domain.model.CustomerType;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.rule.FeeRule;
import com.elgourmat.fee_engine.domain.rule.PercentageFeeRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculateFeesServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-01T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private static final BigDecimal CENT = new BigDecimal("100.00");
    private static final CalculateFeesCommand CMD =
            new CalculateFeesCommand(CENT, "MAD", "STANDARD", "ONLINE", "MA");

    @Test
    void retourne_le_breakdown_calcule_par_le_moteur() {
        FakeRuleProvider provider = new FakeRuleProvider(List.of(
                new PercentageFeeRule("Commission", new BigDecimal("0.015"))));
        FakeFeeCalculationRepository repo = new FakeFeeCalculationRepository();
        CalculateFeesService service = new CalculateFeesService(provider, repo, FIXED_CLOCK);

        FeeBreakdown result = service.handle(CMD);

        assertEquals(Money.of(new BigDecimal("1.50"), Currency.MAD), result.totalFees());
    }

    @Test
    void charge_les_regles_via_le_provider_a_chaque_appel() {
        FakeRuleProvider provider = new FakeRuleProvider(List.of());
        FakeFeeCalculationRepository repo = new FakeFeeCalculationRepository();
        CalculateFeesService service = new CalculateFeesService(provider, repo, FIXED_CLOCK);

        service.handle(CMD);
        service.handle(CMD);

        assertEquals(2, provider.loadCount);
    }

    @Test
    void persiste_le_record_apres_calcul() {
        FakeRuleProvider provider = new FakeRuleProvider(List.of(
                new PercentageFeeRule("Commission", new BigDecimal("0.015"))));
        FakeFeeCalculationRepository repo = new FakeFeeCalculationRepository();
        CalculateFeesService service = new CalculateFeesService(provider, repo, FIXED_CLOCK);

        service.handle(CMD);

        assertNotNull(repo.lastSaved);
        assertEquals(NOW, repo.lastSaved.calculatedAt());
        assertEquals(Money.of(new BigDecimal("1.50"), Currency.MAD),
                repo.lastSaved.breakdown().totalFees());
    }

    @Test
    void normalise_les_enums_en_majuscules() {
        FakeRuleProvider provider = new FakeRuleProvider(List.of());
        FakeFeeCalculationRepository repo = new FakeFeeCalculationRepository();
        CalculateFeesService service = new CalculateFeesService(provider, repo, FIXED_CLOCK);

        CalculateFeesCommand lowercase = new CalculateFeesCommand(
                CENT, "mad", "standard", "online", "MA");

        service.handle(lowercase);

        assertEquals(Currency.MAD, repo.lastSaved.transaction().currency());
        assertEquals(CustomerType.STANDARD, repo.lastSaved.transaction().customerType());
        assertEquals(Channel.ONLINE, repo.lastSaved.transaction().channel());
    }

    @Test
    void propage_exception_pour_currency_inconnue() {
        FakeRuleProvider provider = new FakeRuleProvider(List.of());
        FakeFeeCalculationRepository repo = new FakeFeeCalculationRepository();
        CalculateFeesService service = new CalculateFeesService(provider, repo, FIXED_CLOCK);

        CalculateFeesCommand bad = new CalculateFeesCommand(CENT, "XYZ", "STANDARD", "ONLINE", "MA");

        assertThrows(IllegalArgumentException.class, () -> service.handle(bad));
        assertNull(repo.lastSaved);
    }

    @Test
    void refuse_command_null() {
        FakeRuleProvider provider = new FakeRuleProvider(List.of());
        FakeFeeCalculationRepository repo = new FakeFeeCalculationRepository();
        CalculateFeesService service = new CalculateFeesService(provider, repo, FIXED_CLOCK);

        assertThrows(NullPointerException.class, () -> service.handle(null));
    }

    @Test
    void refuse_dependance_null_au_constructeur() {
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesService(null, new FakeFeeCalculationRepository(), FIXED_CLOCK));
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesService(new FakeRuleProvider(List.of()), null, FIXED_CLOCK));
        assertThrows(NullPointerException.class,
                () -> new CalculateFeesService(
                        new FakeRuleProvider(List.of()),
                        new FakeFeeCalculationRepository(),
                        null));
    }

    // ---- Fakes hand-rolled (pas de Mockito) ----

    static final class FakeRuleProvider implements RuleProvider {
        private final List<FeeRule> rules;
        int loadCount = 0;

        FakeRuleProvider(List<FeeRule> rules) {
            this.rules = new ArrayList<>(rules);
        }

        @Override
        public List<FeeRule> load() {
            loadCount++;
            return rules;
        }
    }

    static final class FakeFeeCalculationRepository implements FeeCalculationRepository {
        FeeCalculationRecord lastSaved;

        @Override
        public void save(FeeCalculationRecord record) {
            this.lastSaved = record;
        }
    }
}

package com.elgourmat.fee_engine.application.service;

import com.elgourmat.fee_engine.application.command.CalculateFeesCommand;
import com.elgourmat.fee_engine.application.model.FeeCalculationRecord;
import com.elgourmat.fee_engine.application.port.in.CalculateFeesUseCase;
import com.elgourmat.fee_engine.application.port.out.FeeCalculationRepository;
import com.elgourmat.fee_engine.application.port.out.RuleProvider;
import com.elgourmat.fee_engine.domain.model.Channel;
import com.elgourmat.fee_engine.domain.model.Currency;
import com.elgourmat.fee_engine.domain.model.CustomerType;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.Money;
import com.elgourmat.fee_engine.domain.model.Transaction;
import com.elgourmat.fee_engine.domain.service.FeeEngine;

import java.time.Clock;
import java.util.Objects;

public class CalculateFeesService implements CalculateFeesUseCase {

    private final RuleProvider ruleProvider;
    private final FeeCalculationRepository repository;
    private final Clock clock;

    public CalculateFeesService(RuleProvider ruleProvider,
                                FeeCalculationRepository repository,
                                Clock clock) {
        this.ruleProvider = Objects.requireNonNull(ruleProvider, "ruleProvider required");
        this.repository = Objects.requireNonNull(repository, "repository required");
        this.clock = Objects.requireNonNull(clock, "clock required");
    }

    @Override
    public FeeBreakdown handle(CalculateFeesCommand command) {
        Objects.requireNonNull(command, "command required");
        Transaction tx = toTransaction(command);
        FeeEngine engine = new FeeEngine(ruleProvider.load());
        FeeBreakdown breakdown = engine.calculate(tx);
        repository.save(FeeCalculationRecord.of(tx, breakdown, clock.instant()));
        return breakdown;
    }

    private Transaction toTransaction(CalculateFeesCommand cmd) {
        Currency currency = Currency.valueOf(cmd.currency().toUpperCase());
        CustomerType customerType = CustomerType.valueOf(cmd.customerType().toUpperCase());
        Channel channel = Channel.valueOf(cmd.channel().toUpperCase());
        Money amount = Money.of(cmd.amount(), currency);
        return new Transaction(amount, customerType, channel, cmd.countryCode());
    }
}

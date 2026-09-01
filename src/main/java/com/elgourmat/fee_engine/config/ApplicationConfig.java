package com.elgourmat.fee_engine.config;

import com.elgourmat.fee_engine.application.port.in.CalculateFeesUseCase;
import com.elgourmat.fee_engine.application.port.out.FeeCalculationRepository;
import com.elgourmat.fee_engine.application.port.out.RuleProvider;
import com.elgourmat.fee_engine.application.service.CalculateFeesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CalculateFeesUseCase calculateFeesUseCase(RuleProvider ruleProvider,
            FeeCalculationRepository repository,
            Clock clock) {
        return new CalculateFeesService(ruleProvider, repository, clock);
    }
}

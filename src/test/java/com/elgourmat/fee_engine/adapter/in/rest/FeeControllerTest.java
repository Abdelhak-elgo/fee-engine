package com.elgourmat.fee_engine.adapter.in.rest;

import com.elgourmat.fee_engine.adapter.in.rest.error.GlobalExceptionHandler;
import com.elgourmat.fee_engine.adapter.in.rest.mapper.FeeRestMapperImpl;
import com.elgourmat.fee_engine.application.command.CalculateFeesCommand;
import com.elgourmat.fee_engine.application.port.in.CalculateFeesUseCase;
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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeeController.class)
@Import({GlobalExceptionHandler.class, FeeRestMapperImpl.class})
class FeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculateFeesUseCase calculateFeesUseCase;

    @Test
    @DisplayName("POST /calculate — cas nominal renvoie 200 avec breakdown")
    void POST_calculate_cas_nominal_200() throws Exception {
        Transaction tx = new Transaction(
                Money.of(new BigDecimal("100.00"), Currency.MAD),
                CustomerType.STANDARD, Channel.ONLINE, "MA");
        FeeBreakdown breakdown = FeeBreakdown.empty(tx)
                .withLine(FeeLine.charge("Commission 1.5%",
                        Money.of(new BigDecimal("1.50"), Currency.MAD),
                        "Commission"));

        when(calculateFeesUseCase.handle(any(CalculateFeesCommand.class))).thenReturn(breakdown);

        String body = """
                {
                  "amount": 100.00,
                  "currency": "mad",
                  "customerType": "standard",
                  "channel": "online",
                  "countryCode": "ma"
                }
                """;

        mockMvc.perform(post("/api/v1/fees/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("MAD"))
                .andExpect(jsonPath("$.transactionAmount").value(100.00))
                .andExpect(jsonPath("$.lines[0].ruleName").value("Commission 1.5%"))
                .andExpect(jsonPath("$.lines[0].type").value("CHARGE"))
                .andExpect(jsonPath("$.totalFees").value(1.50))
                .andExpect(jsonPath("$.grandTotal").value(101.50));
    }

    @Test
    @DisplayName("POST /calculate — validation échouée renvoie 400")
    void POST_calculate_validation_échouée_400() throws Exception {
        String body = """
                {
                  "amount": null,
                  "currency": "",
                  "customerType": "",
                  "channel": "",
                  "countryCode": "FRA"
                }
                """;

        mockMvc.perform(post("/api/v1/fees/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /calculate — devise inconnue renvoie 400")
    void POST_calculate_devise_inconnue_400() throws Exception {
        when(calculateFeesUseCase.handle(any(CalculateFeesCommand.class)))
                .thenThrow(new IllegalArgumentException("No enum constant Currency.XXX"));

        String body = """
                {
                  "amount": 100.00,
                  "currency": "XXX",
                  "customerType": "STANDARD",
                  "channel": "ONLINE",
                  "countryCode": "MA"
                }
                """;

        mockMvc.perform(post("/api/v1/fees/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}

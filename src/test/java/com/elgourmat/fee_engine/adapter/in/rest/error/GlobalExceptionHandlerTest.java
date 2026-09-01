package com.elgourmat.fee_engine.adapter.in.rest.error;

import com.elgourmat.fee_engine.adapter.in.rest.FeeController;
import com.elgourmat.fee_engine.adapter.in.rest.mapper.FeeRestMapper;
import com.elgourmat.fee_engine.application.command.CalculateFeesCommand;
import com.elgourmat.fee_engine.application.port.in.CalculateFeesUseCase;
import com.elgourmat.fee_engine.domain.exception.CurrencyMismatchException;
import com.elgourmat.fee_engine.domain.exception.InvalidAmountException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeeController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    private static final String VALID_BODY = """
            {
              "amount": 100.00,
              "currency": "MAD",
              "customerType": "STANDARD",
              "channel": "ONLINE",
              "countryCode": "MA"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculateFeesUseCase calculateFeesUseCase;

    @MockitoBean
    private FeeRestMapper mapper;

    @Test
    @DisplayName("retourne 400 ProblemDetail pour InvalidAmount")
    void retourne_400_ProblemDetail_pour_InvalidAmount() throws Exception {
        when(mapper.toCommand(any())).thenReturn(new CalculateFeesCommand(
                new BigDecimal("100.00"), "MAD", "STANDARD", "ONLINE", "MA"));
        when(calculateFeesUseCase.handle(any()))
                .thenThrow(new InvalidAmountException("Le montant ne peut pas être négatif"));

        mockMvc.perform(post("/api/v1/fees/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid amount"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("retourne 400 avec liste errors pour validation")
    void retourne_400_avec_liste_errors_pour_validation() throws Exception {
        String invalidBody = """
                {
                  "amount": null,
                  "currency": "",
                  "customerType": "",
                  "channel": "",
                  "countryCode": ""
                }
                """;

        mockMvc.perform(post("/api/v1/fees/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("retourne 400 pour CurrencyMismatch")
    void retourne_400_pour_CurrencyMismatch() throws Exception {
        when(mapper.toCommand(any())).thenReturn(new CalculateFeesCommand(
                new BigDecimal("100.00"), "MAD", "STANDARD", "ONLINE", "MA"));
        when(calculateFeesUseCase.handle(any()))
                .thenThrow(new CurrencyMismatchException("Devises incompatibles : MAD et EUR"));

        mockMvc.perform(post("/api/v1/fees/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Currency mismatch"));
    }

    @Test
    @DisplayName("Content-Type application/problem+json")
    void content_type_application_problem_json() throws Exception {
        when(mapper.toCommand(any())).thenReturn(new CalculateFeesCommand(
                new BigDecimal("100.00"), "MAD", "STANDARD", "ONLINE", "MA"));
        when(calculateFeesUseCase.handle(any()))
                .thenThrow(new InvalidAmountException("bad amount"));

        mockMvc.perform(post("/api/v1/fees/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }
}

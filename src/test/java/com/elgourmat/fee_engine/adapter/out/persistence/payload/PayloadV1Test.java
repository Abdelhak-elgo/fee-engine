package com.elgourmat.fee_engine.adapter.out.persistence.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayloadV1Test {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("refuse une version différente de 1 à la construction")
    void refuse_version_différente_de_1() {
        assertThatThrownBy(() -> new PayloadV1(2, List.of()))
                .isInstanceOf(UnknownPayloadVersionException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("désérialise un payload v1 correctement")
    void deserialise_payload_v1_correctement() throws Exception {
        String json = """
                {
                  "version": 1,
                  "lines": [
                    {"ruleName":"Commission 1.5%","type":"CHARGE","amount":1.50,"currency":"MAD","reason":"Commission"}
                  ]
                }
                """;

        PayloadV1 payload = mapper.readValue(json, PayloadV1.class);

        assertThat(payload.version()).isEqualTo(1);
        assertThat(payload.lines()).hasSize(1);
        FeeLineJson line = payload.lines().get(0);
        assertThat(line.ruleName()).isEqualTo("Commission 1.5%");
        assertThat(line.type()).isEqualTo("CHARGE");
        assertThat(line.amount()).isEqualByComparingTo("1.50");
        assertThat(line.currency()).isEqualTo("MAD");
        assertThat(line.reason()).isEqualTo("Commission");
    }

    @Test
    @DisplayName("throw sur version inconnue à la désérialisation")
    void throw_sur_version_inconnue() {
        String json = """
                {"version": 99, "lines": []}
                """;

        assertThatThrownBy(() -> mapper.readValue(json, PayloadV1.class))
                .hasRootCauseInstanceOf(UnknownPayloadVersionException.class);
    }

    @Test
    @DisplayName("sérialise un payload v1 en JSON round-trip")
    void serialise_round_trip() throws Exception {
        PayloadV1 original = new PayloadV1(1, List.of(
                new FeeLineJson("Frais fixes", "CHARGE", new BigDecimal("2.00"), "MAD", "Frais canal")));

        String json = mapper.writeValueAsString(original);
        PayloadV1 back = mapper.readValue(json, PayloadV1.class);

        assertThat(back).isEqualTo(original);
    }
}

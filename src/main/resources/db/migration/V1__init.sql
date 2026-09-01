CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE fee_calculation (
    id                     UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_amount     NUMERIC(19,4) NOT NULL,
    transaction_currency   VARCHAR(3)    NOT NULL,
    customer_type          VARCHAR(32)   NOT NULL,
    channel                VARCHAR(32)   NOT NULL,
    country_code           VARCHAR(2)    NOT NULL,
    total_fees             NUMERIC(19,4) NOT NULL,
    grand_total            NUMERIC(19,4) NOT NULL,
    payload_version        SMALLINT      NOT NULL,
    payload                JSONB         NOT NULL,
    created_at             TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_fee_calculation_created_at    ON fee_calculation(created_at);
CREATE INDEX idx_fee_calculation_customer_type ON fee_calculation(customer_type);

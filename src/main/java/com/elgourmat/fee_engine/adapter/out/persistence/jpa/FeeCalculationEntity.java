package com.elgourmat.fee_engine.adapter.out.persistence.jpa;

import com.elgourmat.fee_engine.adapter.out.persistence.payload.PayloadV1;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fee_calculation")
public class FeeCalculationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "transaction_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal transactionAmount;

    @Column(name = "transaction_currency", nullable = false, length = 3)
    private String transactionCurrency;

    @Column(name = "customer_type", nullable = false, length = 32)
    private String customerType;

    @Column(name = "channel", nullable = false, length = 32)
    private String channel;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "total_fees", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFees;

    @Column(name = "grand_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal grandTotal;

    @Column(name = "payload_version", nullable = false)
    private short payloadVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private PayloadV1 payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FeeCalculationEntity() {
    }

    public FeeCalculationEntity(UUID id,
                                BigDecimal transactionAmount,
                                String transactionCurrency,
                                String customerType,
                                String channel,
                                String countryCode,
                                BigDecimal totalFees,
                                BigDecimal grandTotal,
                                short payloadVersion,
                                PayloadV1 payload,
                                Instant createdAt) {
        this.id = id;
        this.transactionAmount = transactionAmount;
        this.transactionCurrency = transactionCurrency;
        this.customerType = customerType;
        this.channel = channel;
        this.countryCode = countryCode;
        this.totalFees = totalFees;
        this.grandTotal = grandTotal;
        this.payloadVersion = payloadVersion;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public String getTransactionCurrency() { return transactionCurrency; }
    public String getCustomerType() { return customerType; }
    public String getChannel() { return channel; }
    public String getCountryCode() { return countryCode; }
    public BigDecimal getTotalFees() { return totalFees; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public short getPayloadVersion() { return payloadVersion; }
    public PayloadV1 getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}

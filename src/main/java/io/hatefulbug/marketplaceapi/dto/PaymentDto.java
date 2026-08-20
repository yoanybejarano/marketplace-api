package io.hatefulbug.marketplaceapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.hatefulbug.marketplaceapi.enums.PaymentGatewayType;
import io.hatefulbug.marketplaceapi.enums.PaymentMethod;
import io.hatefulbug.marketplaceapi.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDto {
    private Integer id;
    private OrderDto order;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private PaymentGatewayType gateway;
    private String gatewayMessage;
    private BigDecimal amount;
    private String currency;
    private Instant paymentDate;
    private Instant authorizedAt;
    private Instant capturedAt;
    private Instant refundedAt;
}


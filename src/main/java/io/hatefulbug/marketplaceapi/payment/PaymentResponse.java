package io.hatefulbug.marketplaceapi.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.hatefulbug.marketplaceapi.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentResponse {

    private String paymentId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;
    private String message;

}

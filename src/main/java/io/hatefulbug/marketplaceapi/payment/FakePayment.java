package io.hatefulbug.marketplaceapi.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.hatefulbug.marketplaceapi.enums.PaymentMethod;
import io.hatefulbug.marketplaceapi.enums.PaymentStatus;
import lombok.Data;

@Data
public class FakePayment {
    private String id;
    private Integer orderId;
    private Integer customerId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}

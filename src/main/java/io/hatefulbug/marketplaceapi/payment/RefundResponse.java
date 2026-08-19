package io.hatefulbug.marketplaceapi.payment;

import io.hatefulbug.marketplaceapi.enums.PaymentStatus;
import lombok.Data;

@Data
public class RefundResponse {

    private String paymentId;
    private PaymentStatus status;
    private String message;

}

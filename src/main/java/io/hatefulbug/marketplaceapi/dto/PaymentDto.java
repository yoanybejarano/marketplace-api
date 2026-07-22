package io.hatefulbug.marketplaceapi.dto;

import java.time.Instant;

public record PaymentDto(
        Integer id,
        OrderDto order,
        String paymentMethod,
        String paymentStatus,
        String transactionId,
        Instant paymentDate
) {}

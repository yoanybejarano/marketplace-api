package io.hatefulbug.marketplaceapi.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        Integer id,
        CustomerDto customer,
        Instant orderDate,
        String status,
        BigDecimal totalAmount,
        List<OrderItemDto> orderItems
) {

    public OrderDto {
        orderItems = orderItems == null
                ? List.of()
                : List.copyOf(orderItems);
    }
}

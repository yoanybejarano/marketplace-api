package io.hatefulbug.marketplaceapi.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Integer id,
        ProductDto product,
        Integer quantity,
        BigDecimal unitPrice
) {}

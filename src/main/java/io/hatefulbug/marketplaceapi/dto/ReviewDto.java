package io.hatefulbug.marketplaceapi.dto;

import java.time.Instant;

public record ReviewDto(
        Integer id,
        CustomerDto customer,
        ProductDto product,
        Integer rating,
        String comment,
        Instant createdAt
) {
}

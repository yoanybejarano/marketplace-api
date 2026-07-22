package io.hatefulbug.marketplaceapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductDto(
        Integer id,
        CategoryDto category,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String sku,
        String imageUrl,
        Instant createdAt
) {}

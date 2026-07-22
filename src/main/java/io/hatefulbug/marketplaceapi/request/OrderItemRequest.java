package io.hatefulbug.marketplaceapi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Order Item Request information")
public record OrderItemRequest(
        @Schema(description = "Product ID", example = "1")
        @NotNull(message = "Product ID is required")
        @Min(value = 1, message = "Product ID must be greater than zero")
        Integer productId,

        @Schema(description = "Quantity", example = "5")
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}

package io.hatefulbug.marketplaceapi.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order Item Request information")
public record OrderItemRequest(
        @Schema(description = "Product ID", example = "1")
        @NotNull(message = "Product ID is required")
        @Min(value = 1, message = "Product ID must be greater than zero")
        Integer productId,

        @Schema(description = "Location ID", example = "1")
        @NotNull(message = "Location ID is required")
        @Min(value = 1, message = "Location ID must be greater than zero")
        Integer locationId,

        @Schema(description = "Quantity", example = "5")
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}

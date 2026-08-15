package io.hatefulbug.marketplaceapi.request;

import jakarta.validation.constraints.NotNull;

import io.hatefulbug.marketplaceapi.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status Update request information")
public record StatusUpdateRequest(
        @Schema(description = "Order Status", example = "PROCESSING")
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}

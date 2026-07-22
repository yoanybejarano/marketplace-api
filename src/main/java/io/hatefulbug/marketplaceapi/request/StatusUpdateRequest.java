package io.hatefulbug.marketplaceapi.request;

import io.hatefulbug.marketplaceapi.dto.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Status Update request information")
public record StatusUpdateRequest(
        @Schema(description = "Order Status", example = "PROCESSING")
        @NotNull(message = "Status is required")
        OrderStatus status
) {}

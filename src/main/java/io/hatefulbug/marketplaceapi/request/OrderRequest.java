package io.hatefulbug.marketplaceapi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Order request information")
public record OrderRequest(
        @Schema(description = "Customer ID", example = "1")
        @NotNull(message = "Customer ID is required")
        @Min(value = 1, message = "Customer ID must be greater than zero")
        Integer customerId,

        @Schema(description = "Order Items")
        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid @NotNull OrderItemRequest> items
) {}

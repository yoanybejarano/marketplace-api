package io.hatefulbug.marketplaceapi.payment;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description = "Payment Request information")
public class PaymentRequest {

    @Schema(description = "Amount", example = "1499.99")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount cannot have more than 2 decimal places")
    private BigDecimal amount;

    @Schema(description = "Currency", example = "USD")
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code (e.g., USD, EUR)")
    private String currency;

    @Schema(description = "Payment Method", example = "CREDIT_CARD")
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(description = "Customer ID", example = "1")
    @NotNull(message = "Customer ID is required")
    @Min(value = 1, message = "Customer ID must be greater than zero")
    private Integer customerId;

    @Schema(description = "Order ID", example = "2")
    @NotNull(message = "Order ID is required")
    @Min(value = 1, message = "Order ID must be greater than zero")
    private Integer orderId;

}

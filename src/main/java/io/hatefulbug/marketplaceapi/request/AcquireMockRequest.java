package io.hatefulbug.marketplaceapi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@Schema(description = "Acquire Mock Request information")
public class AcquireMockRequest {
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than zero")
    @Schema(description = "Amount information", example = "250.00")
    private Long amount;

    @NotBlank(message = "Reference cannot be blank")
    @Schema(description = "Internal Order ID")
    private String reference;

    @NotBlank(message = "Webhook URL is required")
    @URL(message = "Webhook URL must be a valid URL")
    @Schema(description = "URL of application to receive the result")
    private String webhookUrl;

    @NotBlank(message = "Redirect URL is required")
    @URL(message = "Redirect URL must be a valid URL")
    @Schema(description = "URL where the customer returns after paying")
    private String redirectUrl;
}

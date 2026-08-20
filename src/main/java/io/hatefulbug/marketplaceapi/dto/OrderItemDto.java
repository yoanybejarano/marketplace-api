package io.hatefulbug.marketplaceapi.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Integer id;
    private ProductDto product;
    private LocationDto location;
    private Integer quantity;
    private BigDecimal unitPrice;
}

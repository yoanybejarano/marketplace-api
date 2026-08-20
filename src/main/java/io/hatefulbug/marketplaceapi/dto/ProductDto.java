package io.hatefulbug.marketplaceapi.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Integer id;
    private CategoryDto category;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private String imageUrl;
    private List<InventoryDto> inventories;
    private Instant createdAt;
    private int stockQuantity;
}

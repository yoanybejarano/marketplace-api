package io.hatefulbug.marketplaceapi.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private Integer id;
    private LocationDto location;
    private Integer quantity;
    private Integer reservedQuantity;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int availableQuantity;

    public boolean hasAvailableStock(int requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }

}


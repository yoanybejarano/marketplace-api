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
public class OrderDto {
    private Integer id;
    private CustomerDto customer;
    private Instant orderDate;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemDto> orderItems;
}

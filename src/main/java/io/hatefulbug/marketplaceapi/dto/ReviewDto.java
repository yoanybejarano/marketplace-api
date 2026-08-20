package io.hatefulbug.marketplaceapi.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Integer id;
    private CustomerDto customer;
    private ProductDto product;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}

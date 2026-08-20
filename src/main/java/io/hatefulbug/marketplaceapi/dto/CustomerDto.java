package io.hatefulbug.marketplaceapi.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Instant createdAt;
}

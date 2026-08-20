package io.hatefulbug.marketplaceapi.dto;

import java.time.Instant;

import io.hatefulbug.marketplaceapi.enums.LocationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    private Integer id;
    private String name;
    private String code;
    private LocationType type;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private boolean active;
    private Instant createdAt;
}


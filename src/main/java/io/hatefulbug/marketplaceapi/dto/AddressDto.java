package io.hatefulbug.marketplaceapi.dto;

public record AddressDto(
        Integer id,
        CustomerDto customer,
        String street,
        String city,
        String state,
        String zipCode,
        String country,
        Boolean isDefault
) {
}

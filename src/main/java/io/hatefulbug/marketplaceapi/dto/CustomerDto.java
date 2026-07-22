package io.hatefulbug.marketplaceapi.dto;

import java.time.Instant;

public record CustomerDto(
        Integer id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Instant createdAt
) {}

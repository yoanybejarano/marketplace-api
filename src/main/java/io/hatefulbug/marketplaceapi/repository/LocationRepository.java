package io.hatefulbug.marketplaceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hatefulbug.marketplaceapi.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Integer> {
}

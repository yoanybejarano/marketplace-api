package io.hatefulbug.marketplaceapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hatefulbug.marketplaceapi.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByProductIdAndLocationId(
            Integer productId,
            Integer locationId
    );
}

package io.hatefulbug.marketplaceapi.repository;

import io.hatefulbug.marketplaceapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}

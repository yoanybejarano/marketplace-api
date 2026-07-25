package io.hatefulbug.marketplaceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.hatefulbug.marketplaceapi.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}

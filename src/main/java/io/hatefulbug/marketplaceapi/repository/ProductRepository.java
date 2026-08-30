package io.hatefulbug.marketplaceapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.hatefulbug.marketplaceapi.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = {"category", "inventories"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "inventories"})
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
}

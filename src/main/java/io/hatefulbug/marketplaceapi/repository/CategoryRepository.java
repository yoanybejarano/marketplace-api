package io.hatefulbug.marketplaceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.hatefulbug.marketplaceapi.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}

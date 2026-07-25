package io.hatefulbug.marketplaceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.hatefulbug.marketplaceapi.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
}

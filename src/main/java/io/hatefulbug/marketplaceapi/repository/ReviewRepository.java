package io.hatefulbug.marketplaceapi.repository;

import io.hatefulbug.marketplaceapi.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
}

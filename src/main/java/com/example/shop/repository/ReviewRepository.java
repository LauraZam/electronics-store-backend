package com.example.shop.repository;

import com.example.shop.model.entity.Review;
import com.example.shop.model.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = com.example.shop.model.enums.ReviewStatus.APPROVED")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    Integer countByProductIdAndStatus(Long productId, ReviewStatus status);
}
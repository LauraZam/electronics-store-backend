package com.example.shop.repository;

import com.example.shop.model.entity.Review;
import com.example.shop.model.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
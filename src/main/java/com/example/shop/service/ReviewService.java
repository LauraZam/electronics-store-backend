package com.example.shop.service;

import com.example.shop.dto.request.CreateReviewRequest;
import com.example.shop.dto.response.ReviewResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.Product;
import com.example.shop.model.entity.Review;
import com.example.shop.model.entity.User;
import com.example.shop.model.enums.ReviewStatus;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ReviewRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ReviewResponse addReview(Long productId, String email, CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isVerified = orderRepository.findByUserId(user.getId(), Pageable.unpaged()).stream()
                .flatMap(o -> o.getOrderItems().stream())
                .anyMatch(i -> i.getProduct().getId().equals(productId));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());
        review.setStatus(ReviewStatus.APPROVED);
        review.setIsVerifiedPurchase(isVerified);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        recalculateProductRating(product);

        return mapToResponse(saved);
    }

    private void recalculateProductRating(Product product) {
        Double avg = reviewRepository.getAverageRatingByProductId(product.getId());
        Integer count = reviewRepository.countByProductIdAndStatus(product.getId(), ReviewStatus.APPROVED);
        product.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        product.setReviewCount(count != null ? count : 0);
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(Review r) {
        return new ReviewResponse(
                r.getId(), r.getProduct().getId(),
                r.getUser().getFirstName() + " " + r.getUser().getLastName(),
                r.getRating(), r.getTitle(), r.getComment(), r.getIsVerifiedPurchase(), r.getCreatedAt()
        );
    }
}
package com.example.shop.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long productId,
        String userFullName,
        Integer rating,
        String title,
        String comment,
        Boolean isVerifiedPurchase,
        LocalDateTime createdAt
) {}
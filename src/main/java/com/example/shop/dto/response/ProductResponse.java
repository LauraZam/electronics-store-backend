package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal basePrice,
        BigDecimal discountPrice,
        Integer stockQuantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        Boolean isActive,
        Boolean isFeatured,
        Boolean isNewArrival,
        Double averageRating,
        Integer reviewCount,
        Long categoryId,
        String categoryName,
        Long brandId,
        String brandName,
        String description,
        LocalDateTime createdAt
) {}
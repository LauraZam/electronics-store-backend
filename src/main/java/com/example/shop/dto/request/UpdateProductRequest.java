package com.example.shop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        String name,

        @Positive(message = "Base price must be positive")
        BigDecimal basePrice,

        BigDecimal discountPrice,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        Long categoryId,
        Long brandId,
        String description,
        Boolean isFeatured,
        Boolean isNewArrival
) {}
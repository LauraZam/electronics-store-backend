package com.example.shop.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        String name,

        @NotBlank(message = "SKU is required")
        @Size(max = 64, message = "SKU must not exceed 64 characters")
        String sku,

        @NotNull(message = "Base price is required")
        @Positive(message = "Base price must be positive")
        BigDecimal basePrice,

        BigDecimal discountPrice,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        Long brandId,
        String description,
        Boolean isFeatured,
        Boolean isNewArrival
) {}
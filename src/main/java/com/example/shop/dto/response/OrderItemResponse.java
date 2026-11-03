package com.example.shop.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal priceAtPurchase,
        BigDecimal totalPrice
) {}
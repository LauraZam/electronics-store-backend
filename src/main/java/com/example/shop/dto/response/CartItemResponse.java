package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal priceAtAdd,
        BigDecimal currentPrice,
        Boolean priceChanged,
        BigDecimal totalPrice
) {}
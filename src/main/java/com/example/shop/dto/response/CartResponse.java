package com.example.shop.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        String sessionId,
        String appliedPromoCode,
        List<CartItemResponse> items,
        BigDecimal subtotal
) {}
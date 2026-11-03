package com.example.shop.dto.response;

import com.example.shop.model.enums.DiscountType;
import java.math.BigDecimal;

public record PromoCodeResponse(
        Long id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal calculatedDiscount,
        Boolean isValid
) {}
package com.example.shop.dto.request;

import com.example.shop.model.enums.DeliveryMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "Delivery method is required")
        DeliveryMethod deliveryMethod,

        Long deliveryOptionId,
        String promoCode,

        @Min(value = 0, message = "Bonus points used cannot be negative")
        Integer bonusPointsUsed,

        String customerNote
) {}
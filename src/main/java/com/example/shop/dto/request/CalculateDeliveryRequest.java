package com.example.shop.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CalculateDeliveryRequest(
        @NotNull(message = "Delivery option ID is required")
        Long deliveryOptionId,

        @NotNull(message = "Order subtotal is required")
        BigDecimal subtotal
) {}
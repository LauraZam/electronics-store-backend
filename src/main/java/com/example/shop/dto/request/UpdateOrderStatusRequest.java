package com.example.shop.dto.request;

import com.example.shop.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}
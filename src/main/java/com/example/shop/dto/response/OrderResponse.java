package com.example.shop.dto.response;

import com.example.shop.model.enums.DeliveryMethod;
import com.example.shop.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        DeliveryMethod deliveryMethod,
        String deliveryTrackingNumber,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal discount,
        Integer bonusPointsUsed,
        BigDecimal totalAmount,
        String promoCode,
        String customerNote,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {}
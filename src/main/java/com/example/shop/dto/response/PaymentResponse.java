package com.example.shop.dto.response;

import com.example.shop.model.enums.PaymentMethod;
import com.example.shop.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String transactionId,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        BigDecimal amount,
        String cardLastFourDigits,
        String errorMessage,
        LocalDateTime paidAt
) {}
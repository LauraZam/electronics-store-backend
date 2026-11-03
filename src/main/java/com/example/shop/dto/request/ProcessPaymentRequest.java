package com.example.shop.dto.request;

import com.example.shop.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProcessPaymentRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^\\d{13,19}$", message = "Invalid card number length")
        String cardNumber,

        @NotBlank(message = "Expiry month is required")
        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expiry month must be MM (01-12)")
        String expiryMonth,

        @NotBlank(message = "Expiry year is required")
        @Pattern(regexp = "^\\d{2}$", message = "Expiry year must be YY")
        String expiryYear,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 digits")
        String cvv
) {}
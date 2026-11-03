package com.example.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ValidatePromoRequest(
        @NotBlank String code,
        @NotNull BigDecimal subtotal
) {}
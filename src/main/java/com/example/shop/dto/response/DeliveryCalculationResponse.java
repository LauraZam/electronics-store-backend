package com.example.shop.dto.response;

import java.math.BigDecimal;

public record DeliveryCalculationResponse(
        Long deliveryOptionId,
        String deliveryOptionName,
        BigDecimal calculatedCost,
        Boolean isFreeShipping,
        Integer estimatedDaysMin,
        Integer estimatedDaysMax
) {}
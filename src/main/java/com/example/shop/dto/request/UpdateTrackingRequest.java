package com.example.shop.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTrackingRequest(
        @NotBlank(message = "Tracking number is required")
        String trackingNumber
) {}
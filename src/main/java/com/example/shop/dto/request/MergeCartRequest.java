package com.example.shop.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MergeCartRequest(
        @NotBlank(message = "Guest session ID is required")
        String sessionId
) {}
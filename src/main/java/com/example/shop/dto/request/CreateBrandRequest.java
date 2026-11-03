package com.example.shop.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBrandRequest(
        @NotBlank(message = "Brand name is required") String name,
        @NotBlank(message = "Slug is required") String slug,
        String description,
        String logoUrl,
        String country
) {}
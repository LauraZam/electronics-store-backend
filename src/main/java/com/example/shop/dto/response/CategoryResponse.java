package com.example.shop.dto.response;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String imageUrl,
        Integer sortOrder,
        Boolean isActive,
        Long parentCategoryId,
        List<CategoryResponse> children
) {}
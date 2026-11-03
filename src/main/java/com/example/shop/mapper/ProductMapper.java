package com.example.shop.mapper;

import com.example.shop.dto.response.ProductResponse;
import com.example.shop.model.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        int available = product.getStockQuantity() - product.getReservedQuantity();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getBasePrice(),
                product.getDiscountPrice(),
                product.getStockQuantity(),
                product.getReservedQuantity(),
                Math.max(0, available),
                product.getIsActive(),
                product.getIsFeatured(),
                product.getIsNewArrival(),
                product.getAverageRating(),
                product.getReviewCount(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getDescription(),
                product.getCreatedAt()
        );
    }
}
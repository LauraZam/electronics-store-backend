package com.example.shop.specification;

import com.example.shop.model.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> filter(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock, Boolean activeOnly) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (Boolean.TRUE.equals(activeOnly)) {
                predicates.getExpressions().add(cb.equal(root.get("isActive"), true));
            }
            if (categoryId != null) {
                predicates.getExpressions().add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }
            if (Boolean.TRUE.equals(inStock)) {
                predicates.getExpressions().add(cb.greaterThan(
                        cb.diff(root.get("stockQuantity"), root.get("reservedQuantity")), 0));
            }
            return predicates;
        };
    }
}
package com.example.shop.controller;

import com.example.shop.dto.response.ProductResponse;
import com.example.shop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public Page<ProductResponse> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 20) Pageable pageable) {
        return products.search(categoryId, minPrice, maxPrice, inStock, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse one(@PathVariable Long id) {
        return products.findById(id);
    }
}
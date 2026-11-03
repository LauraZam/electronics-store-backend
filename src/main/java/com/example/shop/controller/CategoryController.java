package com.example.shop.controller;

import com.example.shop.dto.response.CategoryResponse;
import com.example.shop.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getTree() {
        return categoryService.getCategoryTree();
    }

    @GetMapping("/{id}")
    public CategoryResponse one(@PathVariable Long id) {
        return categoryService.findById(id);
    }
}
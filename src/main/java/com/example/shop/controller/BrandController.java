package com.example.shop.controller;

import com.example.shop.dto.response.BrandResponse;
import com.example.shop.service.BrandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public List<BrandResponse> list() {
        return brandService.findAll();
    }

    @GetMapping("/{id}")
    public BrandResponse one(@PathVariable Long id) {
        return brandService.findById(id);
    }
}
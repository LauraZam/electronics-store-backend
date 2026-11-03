package com.example.shop.service;

import com.example.shop.dto.request.CreateBrandRequest;
import com.example.shop.dto.response.BrandResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.Brand;
import com.example.shop.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAll() {
        return brandRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse findById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return mapToResponse(brand);
    }

    @Transactional
    public BrandResponse create(CreateBrandRequest request) {
        Brand brand = new Brand();
        brand.setName(request.name());
        brand.setSlug(request.slug());
        brand.setDescription(request.description());
        brand.setLogoUrl(request.logoUrl());
        brand.setCountry(request.country());
        brand.setIsActive(true);

        return mapToResponse(brandRepository.save(brand));
    }

    private BrandResponse mapToResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(), brand.getName(), brand.getSlug(),
                brand.getDescription(), brand.getLogoUrl(), brand.getCountry(), brand.getIsActive()
        );
    }
}
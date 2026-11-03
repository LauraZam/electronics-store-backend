package com.example.shop.service;

import com.example.shop.dto.request.CreateProductRequest;
import com.example.shop.dto.request.StockUpdateRequest;
import com.example.shop.dto.request.UpdateProductRequest;
import com.example.shop.dto.response.ProductResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.model.entity.Brand;
import com.example.shop.model.entity.Category;
import com.example.shop.model.entity.Product;
import com.example.shop.repository.BrandRepository;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock, Pageable pageable) {
        var spec = ProductSpecification.filter(categoryId, minPrice, maxPrice, inStock, true);
        return productRepository.findAll(spec, pageable).map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.findBySku(request.sku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + request.sku() + " already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        Brand brand = null;
        if (request.brandId() != null) {
            brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.brandId()));
        }

        Product product = new Product();
        product.setName(request.name());
        product.setSku(request.sku());
        product.setBasePrice(request.basePrice());
        product.setDiscountPrice(request.discountPrice());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);
        product.setBrand(brand);
        product.setDescription(request.description());
        product.setIsFeatured(request.isFeatured() != null ? request.isFeatured() : false);
        product.setIsNewArrival(request.isNewArrival() != null ? request.isNewArrival() : true);
        product.setIsActive(true);

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.name() != null) product.setName(request.name());
        if (request.basePrice() != null) product.setBasePrice(request.basePrice());
        if (request.discountPrice() != null) product.setDiscountPrice(request.discountPrice());
        if (request.stockQuantity() != null) product.setStockQuantity(request.stockQuantity());
        if (request.description() != null) product.setDescription(request.description());
        if (request.isFeatured() != null) product.setIsFeatured(request.isFeatured());
        if (request.isNewArrival() != null) product.setIsNewArrival(request.isNewArrival());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));
            product.setCategory(category);
        }

        if (request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.brandId()));
            product.setBrand(brand);
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void softDelete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse updateStock(Long id, StockUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setStockQuantity(request.stockQuantity());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void setActiveStatus(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setIsActive(active);
        productRepository.save(product);
    }
}
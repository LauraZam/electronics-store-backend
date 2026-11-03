package com.example.shop.service;

import com.example.shop.dto.request.CalculateDeliveryRequest;
import com.example.shop.dto.response.DeliveryCalculationResponse;
import com.example.shop.dto.response.DeliveryOptionResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.DeliveryOption;
import com.example.shop.repository.DeliveryOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryOptionRepository deliveryOptionRepository;

    public DeliveryService(DeliveryOptionRepository deliveryOptionRepository) {
        this.deliveryOptionRepository = deliveryOptionRepository;
    }

    @Transactional(readOnly = true)
    public List<DeliveryOptionResponse> getActiveDeliveryMethods() {
        return deliveryOptionRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeliveryCalculationResponse calculateDeliveryCost(CalculateDeliveryRequest request) {
        DeliveryOption option = deliveryOptionRepository.findById(request.deliveryOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery option not found with id: " + request.deliveryOptionId()));

        boolean isFree = option.getFreeThreshold() != null && request.subtotal().compareTo(option.getFreeThreshold()) >= 0;
        BigDecimal finalCost = isFree ? BigDecimal.ZERO : option.getBasePrice();

        return new DeliveryCalculationResponse(
                option.getId(),
                option.getName(),
                finalCost,
                isFree,
                option.getEstimatedDaysMin(),
                option.getEstimatedDaysMax()
        );
    }

    private DeliveryOptionResponse mapToResponse(DeliveryOption option) {
        return new DeliveryOptionResponse(
                option.getId(),
                option.getName(),
                option.getCode(),
                option.getDescription(),
                option.getBasePrice(),
                option.getFreeThreshold(),
                option.getEstimatedDaysMin(),
                option.getEstimatedDaysMax()
        );
    }
}
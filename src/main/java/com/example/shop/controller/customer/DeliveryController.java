package com.example.shop.controller.customer;

import com.example.shop.dto.request.CalculateDeliveryRequest;
import com.example.shop.dto.response.DeliveryCalculationResponse;
import com.example.shop.dto.response.DeliveryOptionResponse;
import com.example.shop.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/methods")
    public ResponseEntity<List<DeliveryOptionResponse>> getMethods() {
        return ResponseEntity.ok(deliveryService.getActiveDeliveryMethods());
    }

    @PostMapping("/calculate")
    public ResponseEntity<DeliveryCalculationResponse> calculateCost(@Valid @RequestBody CalculateDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.calculateDeliveryCost(request));
    }
}
package com.example.shop.controller.admin;

import com.example.shop.dto.request.UpdateOrderStatusRequest;
import com.example.shop.dto.request.UpdateTrackingRequest;
import com.example.shop.dto.response.OrderResponse;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id, userDetails.getUsername(), true));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    @PutMapping("/{id}/tracking")
    public ResponseEntity<OrderResponse> updateTracking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrackingRequest request) {
        return ResponseEntity.ok(orderService.updateTracking(id, request));
    }
}
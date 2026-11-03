package com.example.shop.controller.customer;

import com.example.shop.dto.request.ProcessPaymentRequest;
import com.example.shop.dto.response.PaymentResponse;
import com.example.shop.model.enums.PaymentMethod;
import com.example.shop.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(userDetails.getUsername(), request));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(orderId, userDetails.getUsername()));
    }

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods() {
        return ResponseEntity.ok(List.of(PaymentMethod.values()));
    }
}
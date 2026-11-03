package com.example.shop.service;

import com.example.shop.dto.request.ProcessPaymentRequest;
import com.example.shop.dto.response.PaymentResponse;
import com.example.shop.exception.ForbiddenException;
import com.example.shop.exception.PaymentFailedException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.Order;
import com.example.shop.model.entity.Payment;
import com.example.shop.model.enums.OrderStatus;
import com.example.shop.model.enums.PaymentStatus;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final Random random = new Random();

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponse processPayment(String email, ProcessPaymentRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.orderId()));

        if (!order.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("Access denied. You do not own this order.");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalArgumentException("Order has already been paid");
        }

        // 1. Validate Card Number with Luhn Algorithm
        if (!isValidLuhn(request.cardNumber())) {
            throw new PaymentFailedException("Invalid credit card number (Luhn check failed)");
        }

        // 2. Simulate Payment Gateway Network Delay (1-3 seconds)
        try {
            int delayMs = 1000 + random.nextInt(2000);
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Evaluate Card Scenarios
        String cleanCard = request.cardNumber().replaceAll("\\s+", "");
        String cardLastFour = cleanCard.substring(cleanCard.length() - 4);
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setOrder(order);
                    return p;
                });

        payment.setPaymentMethod(request.paymentMethod());
        payment.setAmount(order.getTotalAmount());
        payment.setCardLastFourDigits(cardLastFour);
        payment.setTransactionId(transactionId);

        // Scenario Execution
        if ("4111111111111111".equals(cleanCard)) {
            return completeSuccessfulPayment(payment, order);
        } else if ("4000000000000002".equals(cleanCard)) {
            return failPayment(payment, order, "Declined — insufficient funds");
        } else if ("4000000000000069".equals(cleanCard)) {
            return failPayment(payment, order, "Network error");
        } else if ("4000000000000077".equals(cleanCard)) {
            return failPayment(payment, order, "Suspected fraud");
        } else {
            // 90% success, 10% random failure
            boolean success = random.nextInt(100) < 90;
            if (success) {
                return completeSuccessfulPayment(payment, order);
            } else {
                return failPayment(payment, order, "Declined — transaction processing failed");
            }
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("Access denied. You do not own this order.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for order id: " + orderId));

        return mapToResponse(payment);
    }

    private PaymentResponse completeSuccessfulPayment(Payment payment, Order order) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setErrorMessage(null);

        // Set order to CONFIRMED first if it was PENDING, then move to PAID
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        orderRepository.save(order);
        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    private PaymentResponse failPayment(Payment payment, Order order, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorMessage(reason);

        // Failed payment leaves the order in CONFIRMED, never in PAID
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        paymentRepository.save(payment);
        throw new PaymentFailedException("Payment failed: " + reason);
    }

    // Luhn Algorithm Implementation
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getTransactionId(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCardLastFourDigits(),
                payment.getErrorMessage(),
                payment.getPaidAt()
        );
    }
}
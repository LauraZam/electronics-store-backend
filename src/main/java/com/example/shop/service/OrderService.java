package com.example.shop.service;

import com.example.shop.dto.request.CreateOrderRequest;
import com.example.shop.dto.request.UpdateOrderStatusRequest;
import com.example.shop.dto.request.UpdateTrackingRequest;
import com.example.shop.dto.response.OrderItemResponse;
import com.example.shop.dto.response.OrderResponse;
import com.example.shop.exception.ForbiddenException;
import com.example.shop.exception.InsufficientStockException;
import com.example.shop.exception.OrderCancellationException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.*;
import com.example.shop.model.enums.OrderStatus;
import com.example.shop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final DeliveryOptionRepository deliveryOptionRepository;
    private final PromoCodeRepository promoCodeRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            DeliveryOptionRepository deliveryOptionRepository,
            PromoCodeRepository promoCodeRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.deliveryOptionRepository = deliveryOptionRepository;
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create an order with an empty cart");
        }

        // 1. Calculate Subtotal & Reserve Stock
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            int available = product.getStockQuantity() - product.getReservedQuantity();

            if (cartItem.getQuantity() > available) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Reserve stock
            product.setReservedQuantity(product.getReservedQuantity() + cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal unitPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getBasePrice();
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(unitPrice);
            orderItem.setVariant(cartItem.getVariant());
            orderItems.add(orderItem);
        }

        // 2. Calculate Promo Discount
        BigDecimal discount = BigDecimal.ZERO;
        String appliedCode = request.promoCode() != null ? request.promoCode() : cart.getAppliedPromoCode();
        if (appliedCode != null && !appliedCode.isBlank()) {
            discount = calculatePromoDiscount(appliedCode, subtotal);
        }

        // 3. Calculate Shipping Cost
        BigDecimal shippingCost = BigDecimal.ZERO;
        if (request.deliveryOptionId() != null) {
            DeliveryOption deliveryOpt = deliveryOptionRepository.findById(request.deliveryOptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Delivery option not found"));
            if (deliveryOpt.getFreeThreshold() != null && subtotal.compareTo(deliveryOpt.getFreeThreshold()) >= 0) {
                shippingCost = BigDecimal.ZERO;
            } else {
                shippingCost = deliveryOpt.getBasePrice();
            }
        }

        // 4. Calculate Bonus Points Used (Capped at 50% of subtotal - discount)
        int bonusPointsUsed = request.bonusPointsUsed() != null ? request.bonusPointsUsed() : 0;
        BigDecimal netSubtotal = subtotal.subtract(discount);
        BigDecimal maxBonusAllowed = netSubtotal.multiply(new BigDecimal("0.50"));
        BigDecimal requestedBonusValue = BigDecimal.valueOf(bonusPointsUsed).multiply(new BigDecimal("0.01"));

        if (requestedBonusValue.compareTo(maxBonusAllowed) > 0) {
            requestedBonusValue = maxBonusAllowed;
            bonusPointsUsed = maxBonusAllowed.divide(new BigDecimal("0.01"), 0, RoundingMode.DOWN).intValue();
        }

        // 5. Total Arithmetic: totalAmount = subtotal - discount + shippingCost - (bonusUsed * 0.01)
        BigDecimal totalAmount = subtotal.subtract(discount).add(shippingCost).subtract(requestedBonusValue);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // Build Order
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryMethod(request.deliveryMethod());
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingCost(shippingCost);
        order.setBonusPointsUsed(bonusPointsUsed);
        order.setTotalAmount(totalAmount);
        order.setPromoCode(appliedCode);
        order.setCustomerNote(request.customerNote());

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear Cart
        cart.getCartItems().clear();
        cart.setAppliedPromoCode(null);
        cartRepository.save(cart);

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId(), pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String email, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Ownership Check: Customer can only view their own orders
        if (!isAdmin && !order.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("Access denied. You do not own this order.");
        }

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, String email, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (!isAdmin && !order.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("Access denied. You do not own this order.");
        }

        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED);

        // Release stock reservation
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int newReserved = Math.max(0, product.getReservedQuantity() - item.getQuantity());
            product.setReservedQuantity(newReserved);
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        validateStatusTransition(order.getStatus(), request.status());

        order.setStatus(request.status());
        LocalDateTime now = LocalDateTime.now();

        if (request.status() == OrderStatus.PAID) order.setPaidAt(now);
        if (request.status() == OrderStatus.SHIPPED) order.setShippedAt(now);
        if (request.status() == OrderStatus.DELIVERED) order.setDeliveredAt(now);

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateTracking(Long id, UpdateTrackingRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.setDeliveryTrackingNumber(request.trackingNumber());
        return mapToResponse(orderRepository.save(order));
    }

    // State Machine Validator Rule
    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        boolean valid = switch (current) {
            case PENDING -> target == OrderStatus.CONFIRMED || target == OrderStatus.CANCELLED;
            case CONFIRMED -> target == OrderStatus.PAID || target == OrderStatus.CANCELLED;
            case PAID -> target == OrderStatus.PROCESSING || target == OrderStatus.REFUNDED;
            case PROCESSING -> target == OrderStatus.SHIPPED || target == OrderStatus.CANCELLED;
            case SHIPPED -> target == OrderStatus.IN_TRANSIT;
            case IN_TRANSIT -> target == OrderStatus.DELIVERED;
            case DELIVERED -> target == OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };

        if (!valid) {
            throw new OrderCancellationException("Illegal order status transition from " + current + " to " + target);
        }
    }

    private BigDecimal calculatePromoDiscount(String code, BigDecimal subtotal) {
        return promoCodeRepository.findByCode(code)
                .map(promo -> {
                    if (!promo.getIsActive()) return BigDecimal.ZERO;
                    if (promo.getMinOrderAmount() != null && subtotal.compareTo(promo.getMinOrderAmount()) < 0) return BigDecimal.ZERO;

                    BigDecimal discount = switch (promo.getDiscountType()) {
                        case PERCENTAGE -> subtotal.multiply(promo.getDiscountValue().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                        case FIXED_AMOUNT -> promo.getDiscountValue();
                        case FREE_SHIPPING -> BigDecimal.ZERO;
                    };

                    if (promo.getMaxDiscountAmount() != null && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                        discount = promo.getMaxDiscountAmount();
                    }
                    return discount;
                })
                .orElse(BigDecimal.ZERO);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getDeliveryMethod(),
                order.getDeliveryTrackingNumber(),
                order.getSubtotal(),
                order.getShippingCost(),
                order.getDiscount(),
                order.getBonusPointsUsed(),
                order.getTotalAmount(),
                order.getPromoCode(),
                order.getCustomerNote(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}
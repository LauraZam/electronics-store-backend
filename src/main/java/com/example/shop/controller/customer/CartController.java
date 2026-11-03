package com.example.shop.controller.customer;

import com.example.shop.dto.request.AddToCartRequest;
import com.example.shop.dto.request.MergeCartRequest;
import com.example.shop.dto.request.UpdateCartItemRequest;
import com.example.shop.dto.response.CartResponse;
import com.example.shop.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(cartService.getCart(email, sessionId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @Valid @RequestBody AddToCartRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(cartService.addItem(email, sessionId, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(cartService.updateItemQuantity(email, sessionId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable Long itemId) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(cartService.removeItem(email, sessionId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        cartService.clearCart(email, sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeGuestCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MergeCartRequest request) {
        return ResponseEntity.ok(cartService.mergeGuestCart(userDetails.getUsername(), request.sessionId()));
    }
}
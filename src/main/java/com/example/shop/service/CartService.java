package com.example.shop.service;

import com.example.shop.dto.request.AddToCartRequest;
import com.example.shop.dto.request.UpdateCartItemRequest;
import com.example.shop.dto.response.CartItemResponse;
import com.example.shop.dto.response.CartResponse;
import com.example.shop.exception.InsufficientStockException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.Cart;
import com.example.shop.model.entity.CartItem;
import com.example.shop.model.entity.Product;
import com.example.shop.model.entity.User;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse getCart(String email, String sessionId) {
        Cart cart = getOrCreateCart(email, sessionId);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String email, String sessionId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(email, sessionId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        int availableStock = product.getStockQuantity() - product.getReservedQuantity();

        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int targetQuantity = request.quantity();
        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        if (targetQuantity > availableStock) {
            throw new InsufficientStockException("Insufficient stock. Available: " + Math.max(0, availableStock));
        }

        BigDecimal currentPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getBasePrice();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(targetQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.quantity());
            newItem.setPriceAtAdd(currentPrice);
            cart.getCartItems().add(newItem);
        }

        return mapToResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItemQuantity(String email, String sessionId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(email, sessionId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to this cart");
        }

        Product product = item.getProduct();
        int availableStock = product.getStockQuantity() - product.getReservedQuantity();

        if (request.quantity() > availableStock) {
            throw new InsufficientStockException("Insufficient stock. Available: " + Math.max(0, availableStock));
        }

        item.setQuantity(request.quantity());
        return mapToResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(String email, String sessionId, Long itemId) {
        Cart cart = getOrCreateCart(email, sessionId);
        cart.getCartItems().removeIf(item -> item.getId().equals(itemId));
        return mapToResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String email, String sessionId) {
        Cart cart = getOrCreateCart(email, sessionId);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Transactional
    public CartResponse mergeGuestCart(String email, String guestSessionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(guestSessionId);
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getCartItems().isEmpty()) {
            return getCart(email, null);
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return cartRepository.save(c);
                });

        for (CartItem guestItem : guestCart.getCartItems()) {
            Optional<CartItem> userItemOpt = userCart.getCartItems().stream()
                    .filter(i -> i.getProduct().getId().equals(guestItem.getProduct().getId()))
                    .findFirst();

            Product product = guestItem.getProduct();
            int availableStock = product.getStockQuantity() - product.getReservedQuantity();

            if (userItemOpt.isPresent()) {
                CartItem userItem = userItemOpt.get();
                int combinedQty = userItem.getQuantity() + guestItem.getQuantity();
                userItem.setQuantity(Math.min(combinedQty, availableStock));
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(userCart);
                newItem.setProduct(product);
                newItem.setQuantity(Math.min(guestItem.getQuantity(), availableStock));
                newItem.setPriceAtAdd(guestItem.getPriceAtAdd());
                userCart.getCartItems().add(newItem);
            }
        }

        cartRepository.delete(guestCart);
        return mapToResponse(cartRepository.save(userCart));
    }

    private Cart getOrCreateCart(String email, String sessionId) {
        if (email != null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return cartRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setUser(user);
                        return cartRepository.save(cart);
                    });
        } else if (sessionId != null) {
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setSessionId(sessionId);
                        return cartRepository.save(cart);
                    });
        }
        throw new IllegalArgumentException("Either authenticated user or session ID must be provided");
    }

    private CartResponse mapToResponse(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cart.getCartItems()) {
            Product p = item.getProduct();
            BigDecimal currentPrice = p.getDiscountPrice() != null ? p.getDiscountPrice() : p.getBasePrice();
            boolean priceChanged = item.getPriceAtAdd() != null && item.getPriceAtAdd().compareTo(currentPrice) != 0;
            BigDecimal itemTotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            subtotal = subtotal.add(itemTotal);

            itemResponses.add(new CartItemResponse(
                    item.getId(),
                    p.getId(),
                    p.getName(),
                    p.getSku(),
                    item.getQuantity(),
                    item.getPriceAtAdd(),
                    currentPrice,
                    priceChanged,
                    itemTotal
            ));
        }

        return new CartResponse(
                cart.getId(),
                cart.getSessionId(),
                cart.getAppliedPromoCode(),
                itemResponses,
                subtotal
        );
    }
}
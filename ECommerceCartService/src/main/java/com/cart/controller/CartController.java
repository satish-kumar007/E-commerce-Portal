package com.cart.controller;

import com.cart.dto.AddToCartRequest;
import com.cart.dto.CartResponse;
import com.cart.dto.UpdateQuantityRequest;
import com.cart.service.CartService;
import com.cart.client.ProductCatalogClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final ProductCatalogClient productClient;

    public CartController(CartService cartService, ProductCatalogClient productClient) {
        this.cartService = cartService;
        this.productClient = productClient;
    }

    private String resolveUserId(String userIdHeader) {
        if (!StringUtils.hasText(userIdHeader)) {
            throw new IllegalArgumentException("Missing user identity");
        }
        return userIdHeader;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Id") String userIdHeader) {
        String userId = resolveUserId(userIdHeader);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@RequestHeader("X-User-Id") String userIdHeader,
                                                  @Valid @RequestBody AddToCartRequest req) {
        String userId = resolveUserId(userIdHeader);
        CartResponse res = cartService.addToCart(userId, req.getProductId(), req.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/items")
    public ResponseEntity<CartResponse> updateQuantity(@RequestHeader("X-User-Id") String userIdHeader,
                                                       @Valid @RequestBody UpdateQuantityRequest req) {
        String userId = resolveUserId(userIdHeader);
        CartResponse res = cartService.updateQuantity(userId, req.getProductId(), req.getQuantity());
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@RequestHeader("X-User-Id") String userIdHeader,
                                                   @PathVariable("productId") Long productId) {
        String userId = resolveUserId(userIdHeader);
        CartResponse res = cartService.removeItem(userId, productId);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") String userIdHeader) {
        String userId = resolveUserId(userIdHeader);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // Simple checkout validation endpoint (payment integration to be handled by Payment Service)
    @PostMapping("/checkout")
    public ResponseEntity<CartResponse> checkout(@RequestHeader("X-User-Id") String userIdHeader) {
        String userId = resolveUserId(userIdHeader);
        CartResponse cart = cartService.getCart(userId);
        // Validate stock and availability before redirecting to payment
        for (var item : cart.getItems()) {
            var p = productClient.getProductById(item.getProductId());
            if (p == null || !p.isAvailable() || p.getStock() < item.getQuantity()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        // Here we would create an order (PENDING) and proceed to payment; omitted for brevity
        return ResponseEntity.ok(cart);
    }
}

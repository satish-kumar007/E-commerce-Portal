package com.cart.service;

import com.cart.client.ProductCatalogClient;
import com.cart.domain.Cart;
import com.cart.domain.CartItem;
import com.cart.dto.CartItemResponse;
import com.cart.dto.CartResponse;
import com.cart.repository.CartRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final String CART_CACHE_PREFIX = "cart:";

    private final CartRepository cartRepository;
    private final ProductCatalogClient productClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public CartService(CartRepository cartRepository,
                       ProductCatalogClient productClient,
                       RedisTemplate<String, Object> redisTemplate) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.redisTemplate = redisTemplate;
    }

    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(String userId, Long productId, int quantity) {
        final int adjustedQuantity = quantity <= 0 ? 1 : quantity;
        return withOptimisticRetry(userId, cart -> {
            ProductCatalogClient.ProductInfo p = productClient.getProductById(productId);
            if (p == null || !p.isAvailable() || p.getStock() <= 0) {
                throw new IllegalStateException("Product not available or out of stock");
            }
            // If product exists in cart, update quantity, else add new item
            Optional<CartItem> existing = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst();
            if (existing.isPresent()) {
                int newQty = existing.get().getQuantity() + quantity;
                if (newQty > p.getStock()) {
                    throw new IllegalStateException("Insufficient stock for requested quantity");
                }
                existing.get().setQuantity(newQty);
            } else {
                if (quantity > p.getStock()) {
                    throw new IllegalStateException("Insufficient stock for requested quantity");
                }
                cart.getItems().add(new CartItem(
                        p.getId(), p.getName(), p.getSlug(), p.getPrimaryImageUrl(), quantity, p.getPrice()
                ));
            }
            cart.setLastUpdated(Instant.now());
            Cart saved = cartRepository.save(cart);
            cacheCart(saved);
            return toResponse(saved);
        });
    }

    @Transactional
    public CartResponse updateQuantity(String userId, Long productId, int quantity) {
        return withOptimisticRetry(userId, cart -> {
            Optional<CartItem> existing = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst();
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("Item not in cart");
            }
            if (quantity <= 0) {
                cart.getItems().remove(existing.get());
            } else {
                ProductCatalogClient.ProductInfo p = productClient.getProductById(productId);
                if (p == null || !p.isAvailable()) {
                    throw new IllegalStateException("Product unavailable");
                }
                if (quantity > p.getStock()) {
                    throw new IllegalStateException("Insufficient stock");
                }
                existing.get().setQuantity(quantity);
            }
            cart.setLastUpdated(Instant.now());
            Cart saved = cartRepository.save(cart);
            cacheCart(saved);
            return toResponse(saved);
        });
    }

    @Transactional
    public CartResponse removeItem(String userId, Long productId) {
        return withOptimisticRetry(userId, cart -> {
            cart.getItems().removeIf(i -> i.getProductId().equals(productId));
            cart.setLastUpdated(Instant.now());
            Cart saved = cartRepository.save(cart);
            cacheCart(saved);
            return toResponse(saved);
        });
    }

    @Transactional
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cart.setLastUpdated(Instant.now());
        cartRepository.save(cart);
        evictCache(userId);
    }

    private interface CartWork<T> { T apply(Cart cart); }

    private <T> T withOptimisticRetry(String userId, CartWork<T> work) {
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                Cart cart = getOrCreateCart(userId);
                return work.apply(cart);
            } catch (OptimisticLockingFailureException e) {
                if (attempts >= 3) throw e;
                try { Thread.sleep(50L * attempts); } catch (InterruptedException ignored) {}
            }
        }
    }

    private Cart getOrCreateCart(String userId) {
        // Try Redis first
        String key = cacheKey(userId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Cart cachedCart) {
            return cachedCart;
        }
        // DB fallback
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            c.setLastUpdated(Instant.now());
            return cartRepository.save(c);
        });
        cacheCart(cart);
        return cart;
    }

    private void cacheCart(Cart cart) {
        String key = cacheKey(cart.getUserId());
        redisTemplate.opsForValue().set(key, cart, Duration.ofMinutes(30));
    }

    private void evictCache(String userId) {
        redisTemplate.delete(cacheKey(userId));
    }

    private String cacheKey(String userId) {
        return CART_CACHE_PREFIX + userId;
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse res = new CartResponse();
        res.setUserId(cart.getUserId());
        res.setLastUpdated(cart.getLastUpdated());
        List<CartItemResponse> items = cart.getItems().stream().map(i -> {
            CartItemResponse ci = new CartItemResponse();
            ci.setProductId(i.getProductId());
            ci.setName(i.getProductName());
            ci.setSlug(i.getProductSlug());
            ci.setImageUrl(i.getImageUrl());
            ci.setQuantity(i.getQuantity());
            ci.setUnitPriceAtAdd(i.getUnitPriceAtAdd());
            ci.setLineTotal(i.getUnitPriceAtAdd().multiply(BigDecimal.valueOf(i.getQuantity())));
            return ci;
        }).collect(Collectors.toList());
        res.setItems(items);
        res.setTotal(items.stream().map(CartItemResponse::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        return res;
    }
}

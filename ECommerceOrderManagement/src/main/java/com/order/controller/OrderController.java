package com.order.controller;

import com.order.dto.OrderDetailDto;
import com.order.dto.OrderSummaryDto;
import com.order.dto.PlaceOrderRequest;
import com.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private String requireUser(String userHeader) {
        if (!StringUtils.hasText(userHeader)) {
            throw new IllegalArgumentException("Missing user identity");
        }
        return userHeader;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestHeader("X-User-Id") String userHeader,
                                             @Valid @RequestBody PlaceOrderRequest request) {
        String userId = requireUser(userHeader);
        String orderId = orderService.placeOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummaryDto>> history(@RequestHeader("X-User-Id") String userHeader,
                                                         @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                         @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        String userId = requireUser(userHeader);
        Page<OrderSummaryDto> result = orderService.getOrderHistory(userId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailDto> detail(@RequestHeader("X-User-Id") String userHeader,
                                                 @PathVariable("orderId") String orderId) {
        String userId = requireUser(userHeader);
        return orderService.getOrderDetail(userId, orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

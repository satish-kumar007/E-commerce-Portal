package com.order.service;

import com.order.dto.*;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import com.order.entity.OrderTracking;
import com.order.repository.OrderRepository;
import com.order.repository.OrderTrackingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderTrackingRepository trackingRepository;
    private final KafkaTemplate<String, String> kafkaTemplate; // simple skeleton

    public OrderService(OrderRepository orderRepository,
                        OrderTrackingRepository trackingRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.trackingRepository = trackingRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public String placeOrder(String userId, PlaceOrderRequest request) {
        // Generate unique orderId
        String orderId = generateOrderId();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryAddressLine1(request.getDeliveryAddressLine1());
        order.setDeliveryAddressLine2(request.getDeliveryAddressLine2());
        order.setCity(request.getCity());
        order.setState(request.getState());
        order.setZipCode(request.getZipCode());
        order.setCountry(request.getCountry());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDto i : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(i.getProductId());
            item.setProductName(i.getProductName());
            item.setProductSlug(i.getProductSlug());
            item.setQuantity(i.getQuantity());
            item.setUnitPrice(i.getUnitPrice());
            BigDecimal subtotal = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            item.setSubtotal(subtotal);
            total = total.add(subtotal);
            order.addItem(item);
        }
        order.setTotalAmount(total);
        // Assume payment success is handled by Payment service before this call. Mark PAID.
        order.setStatus(Order.Status.PAID);

        Order saved = orderRepository.save(order);

        // Create initial tracking events
        OrderTracking placed = new OrderTracking();
        placed.setOrder(saved);
        placed.setStatus(Order.Status.PAID.name());
        placed.setDescription("Order placed and payment confirmed");
        placed.setEventTime(LocalDateTime.now());
        trackingRepository.save(placed);

        // Publish event for downstream services (inventory, shipping)
        publishOrderPlacedEvent(saved);

        // TODO: send email/SMS notification via Notification service

        return saved.getOrderId();
    }

    private String generateOrderId() {
        String id;
        do {
            id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderId(id));
        return id;
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getOrderHistory(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50), Sort.by(Sort.Direction.DESC, "orderDate"));
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable)
                .map(o -> {
                    OrderSummaryDto s = new OrderSummaryDto();
                    s.setOrderId(o.getOrderId());
                    s.setOrderDate(o.getOrderDate());
                    s.setTotalAmount(o.getTotalAmount());
                    s.setStatus(o.getStatus().name());
                    return s;
                });
    }

    @Transactional(readOnly = true)
    public Optional<OrderDetailDto> getOrderDetail(String userId, String orderId) {
        return orderRepository.findByOrderIdAndUserId(orderId, userId).map(o -> {
            OrderDetailDto d = new OrderDetailDto();
            d.setOrderId(o.getOrderId());
            d.setUserId(o.getUserId());
            d.setOrderDate(o.getOrderDate());
            d.setStatus(o.getStatus().name());
            d.setTotalAmount(o.getTotalAmount());
            d.setPaymentMethod(o.getPaymentMethod());
            d.setDeliveryAddressLine1(o.getDeliveryAddressLine1());
            d.setDeliveryAddressLine2(o.getDeliveryAddressLine2());
            d.setCity(o.getCity());
            d.setState(o.getState());
            d.setZipCode(o.getZipCode());
            d.setCountry(o.getCountry());
            List<OrderItemDto> items = o.getItems().stream().map(i -> {
                OrderItemDto di = new OrderItemDto();
                di.setProductId(i.getProductId());
                di.setProductName(i.getProductName());
                di.setProductSlug(i.getProductSlug());
                di.setQuantity(i.getQuantity());
                di.setUnitPrice(i.getUnitPrice());
                return di;
            }).collect(Collectors.toList());
            d.setItems(items);
            List<TrackingEventDto> ev = trackingRepository.findByOrderOrderByEventTimeAsc(o).stream().map(t -> {
                TrackingEventDto te = new TrackingEventDto();
                te.setStatus(t.getStatus());
                te.setDescription(t.getDescription());
                te.setEventTime(t.getEventTime());
                return te;
            }).collect(Collectors.toList());
            d.setTracking(ev);
            return d;
        });
    }

    private void publishOrderPlacedEvent(Order order) {
        try {
            String payload = "{\"orderId\":\"" + order.getOrderId() + "\",\"userId\":\"" + order.getUserId() + "\",\"total\":" + order.getTotalAmount() + "}";
            kafkaTemplate.send("order-placed", order.getOrderId(), payload);
        } catch (Exception ignored) {
        }
    }
}

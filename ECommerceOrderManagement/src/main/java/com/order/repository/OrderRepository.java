package com.order.repository;

import com.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderIdAndUserId(String orderId, String userId);
    Page<Order> findByUserIdOrderByOrderDateDesc(String userId, Pageable pageable);
    boolean existsByOrderId(String orderId);
}

package com.product.repository;

import com.product.entity.Review;
import com.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findTop10ByProductOrderByCreatedAtDesc(Product product);
    long countByProduct(Product product);
}

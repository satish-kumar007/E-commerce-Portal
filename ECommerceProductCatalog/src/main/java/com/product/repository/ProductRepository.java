package com.product.repository;

import com.product.entity.Product;
import com.product.entity.Category;
import com.product.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);

    Page<Product> findByCategory(Category category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.slug LIKE CONCAT(:slug, '/%') OR p.category.slug = :slug")
    Page<Product> findByCategorySlugTree(@Param("slug") String categorySlug, Pageable pageable);

    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.brand = :brand ORDER BY p.popularity DESC")
    List<Product> findTop10ByBrandOrderByPopularityDesc(@Param("brand") Brand brand);

    @Query("SELECT p FROM Product p WHERE p.category = :category ORDER BY p.popularity DESC")
    List<Product> findTop10ByCategoryOrderByPopularityDesc(@Param("category") Category category);
}

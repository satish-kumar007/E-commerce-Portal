package com.product.spec;

import com.product.entity.Brand;
import com.product.entity.Category;
import com.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {

    public static Specification<Product> hasCategory(Category category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Product> hasBrand(Brand brand) {
        return (root, query, cb) -> cb.equal(root.get("brand"), brand);
    }

    public static Specification<Product> minPrice(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> maxPrice(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<Product> available(boolean available) {
        return (root, query, cb) -> cb.equal(root.get("available"), available);
    }

    public static Specification<Product> minDiscount(BigDecimal minDiscount) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("discountPercent"), minDiscount);
    }

    public static Specification<Product> keyword(String q) {
        if (q == null || q.isBlank()) return (root, query, cb) -> cb.conjunction();
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("description")), like),
                cb.like(cb.lower(root.get("specifications")), like)
        );
    }

    public static Specification<Product> categorySlugTree(String slug) {
        if (slug == null || slug.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.or(
                cb.equal(root.join("category").get("slug"), slug),
                cb.like(root.join("category").get("slug"), slug + "/%")
        );
    }
}

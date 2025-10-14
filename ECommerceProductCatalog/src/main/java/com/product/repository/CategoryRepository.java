package com.product.repository;

import com.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentIsNullOrderByNameAsc();
    List<Category> findByParentIdOrderByNameAsc(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.popular = true ORDER BY c.name ASC")
    List<Category> findPopular();

    @Query("SELECT c FROM Category c WHERE c.slug LIKE CONCAT(:prefix, '/%') OR c.slug = :prefix")
    List<Category> findSubtreeBySlugPrefix(@Param("prefix") String prefix);
}

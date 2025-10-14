package com.product.service;

import com.product.dto.CategoryNodeDto;
import com.product.entity.Category;
import com.product.repository.CategoryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public List<CategoryNodeDto> getRootTree() {
        List<Category> roots = categoryRepository.findByParentIsNullOrderByNameAsc();
        return roots.stream().map(this::toNodeDeep).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "popularCategories")
    @Transactional(readOnly = true)
    public List<CategoryNodeDto> getPopularCategories() {
        List<Category> popular = categoryRepository.findPopular();
        return popular.stream().map(this::toNodeShallow).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryNodeDto> getSubtreeBySlugPrefix(String slugPrefix) {
        List<Category> list = categoryRepository.findSubtreeBySlugPrefix(slugPrefix);
        // Build tree from flat list
        Map<Long, CategoryNodeDto> map = new HashMap<>();
        List<CategoryNodeDto> roots = new ArrayList<>();
        for (Category c : list) {
            CategoryNodeDto node = toNodeShallow(c);
            map.put(c.getId(), node);
        }
        for (Category c : list) {
            CategoryNodeDto node = map.get(c.getId());
            if (c.getParent() != null && map.containsKey(c.getParent().getId())) {
                map.get(c.getParent().getId()).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private CategoryNodeDto toNodeShallow(Category c) {
        CategoryNodeDto n = new CategoryNodeDto();
        n.setId(c.getId());
        n.setName(c.getName());
        n.setSlug(c.getSlug());
        n.setLevel(c.getLevel());
        return n;
    }

    private CategoryNodeDto toNodeDeep(Category c) {
        CategoryNodeDto n = toNodeShallow(c);
        if (c.getChildren() != null) {
            for (Category child : c.getChildren()) {
                n.getChildren().add(toNodeDeep(child));
            }
        }
        return n;
    }
}

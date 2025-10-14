package com.product.controller;

import com.product.dto.CategoryNodeDto;
import com.product.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryNodeDto>> getRootTree() {
        return ResponseEntity.ok(categoryService.getRootTree());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<CategoryNodeDto>> getPopular() {
        return ResponseEntity.ok(categoryService.getPopularCategories());
    }

    @GetMapping("/subtree")
    public ResponseEntity<List<CategoryNodeDto>> getSubtree(@RequestParam("slug") String slugPrefix) {
        return ResponseEntity.ok(categoryService.getSubtreeBySlugPrefix(slugPrefix));
    }
}

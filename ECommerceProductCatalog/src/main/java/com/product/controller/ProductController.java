package com.product.controller;

import com.product.dto.ProductDetailDto;
import com.product.dto.ProductListItemDto;
import com.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/catalog/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Browse by category slug with pagination/sorting/filtering
    @GetMapping
    public ResponseEntity<Page<ProductListItemDto>> browse(
            @RequestParam(value = "category", required = false) String categorySlug,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort, // price_asc, price_desc, newest, rating, popularity
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "minDiscount", required = false) BigDecimal minDiscount,
            @RequestParam(value = "q", required = false) String keyword
    ) {
        Page<ProductListItemDto> pageDto = productService.browseByCategory(categorySlug, page, size, sort, brand, minPrice, maxPrice, available, minDiscount, keyword);
        return ResponseEntity.ok(pageDto);
    }

    // Product detail by slug
    @GetMapping("/{slug}")
    public ResponseEntity<ProductDetailDto> detail(@PathVariable("slug") String slug) {
        Optional<ProductDetailDto> dto = productService.getProductDetail(slug);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Internal: Product detail by ID (for services like Cart/Order)
    @GetMapping("/id/{id}")
    public ResponseEntity<ProductDetailDto> detailById(@PathVariable("id") Long id) {
        Optional<ProductDetailDto> dto = productService.getProductDetailById(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

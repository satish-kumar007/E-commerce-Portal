package com.product.controller;

import com.product.dto.ProductListItemDto;
import com.product.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/catalog/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductListItemDto>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "minDiscount", required = false) BigDecimal minDiscount
    ) {
        Page<ProductListItemDto> pageDto = searchService.search(q, page, size, sort, brand, minPrice, maxPrice, available, minDiscount);
        return ResponseEntity.ok(pageDto);
    }
}

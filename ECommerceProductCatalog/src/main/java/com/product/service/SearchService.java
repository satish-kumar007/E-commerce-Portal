package com.product.service;

import com.product.dto.ProductListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SearchService {

    private final ProductService productService;

    public SearchService(ProductService productService) {
        this.productService = productService;
    }

    public Page<ProductListItemDto> search(String keyword,
                                           Integer page,
                                           Integer size,
                                           String sort,
                                           String brand,
                                           BigDecimal minPrice,
                                           BigDecimal maxPrice,
                                           Boolean available,
                                           BigDecimal minDiscount) {
        return productService.browseByCategory(null, page, size, sort, brand, minPrice, maxPrice, available, minDiscount, keyword);
    }
}

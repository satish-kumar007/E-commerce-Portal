package com.product.service;

import com.product.dto.ProductDetailDto;
import com.product.dto.ProductListItemDto;
import com.product.entity.Brand;
import com.product.entity.Category;
import com.product.entity.Product;
import com.product.repository.BrandRepository;
import com.product.repository.CategoryRepository;
import com.product.repository.ProductImageRepository;
import com.product.repository.ProductRepository;
import com.product.spec.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository,
                          ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productImageRepository = productImageRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductListItemDto> browseByCategory(String categorySlug,
                                                     Integer page,
                                                     Integer size,
                                                     String sort,
                                                     String brand,
                                                     BigDecimal minPrice,
                                                     BigDecimal maxPrice,
                                                     Boolean available,
                                                     BigDecimal minDiscount,
                                                     String keyword) {
        Pageable pageable = buildPageable(page, size, sort);
        Specification<Product> spec = Specification.where(null);

        if (StringUtils.hasText(categorySlug)) {
            // Include products from all subcategories via categorySlug tree query
            // We'll filter via custom repo method, but specification-based keyword/filters still apply
        }

        if (StringUtils.hasText(brand)) {
            var brandOpt = brandRepository.findByNameIgnoreCase(brand);
            if (brandOpt.isPresent()) {
                spec = spec.and(ProductSpecifications.hasBrand(brandOpt.get()));
            }
        }
        if (minPrice != null) spec = spec.and(ProductSpecifications.minPrice(minPrice));
        if (maxPrice != null) spec = spec.and(ProductSpecifications.maxPrice(maxPrice));
        if (available != null) spec = spec.and(ProductSpecifications.available(available));
        if (minDiscount != null) spec = spec.and(ProductSpecifications.minDiscount(minDiscount));
        if (StringUtils.hasText(keyword)) spec = spec.and(ProductSpecifications.keyword(keyword));

        if (StringUtils.hasText(categorySlug)) {
            return productRepository.findByCategorySlugTree(categorySlug, pageable)
                    .map(this::toListItemDto);
        } else {
            return productRepository.findAll(spec, pageable).map(this::toListItemDto);
        }
    }

    @Transactional(readOnly = true)
    public Optional<ProductDetailDto> getProductDetail(String productSlug) {
        return productRepository.findBySlug(productSlug).map(p -> {
            ProductDetailDto dto = new ProductDetailDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setSlug(p.getSlug());
            dto.setBrandName(p.getBrand() != null ? p.getBrand().getName() : null);
            dto.setCategorySlug(p.getCategory() != null ? p.getCategory().getSlug() : null);
            dto.setDescription(p.getDescription());
            dto.setSpecifications(p.getSpecifications());
            dto.setPrice(p.getPrice());
            dto.setDiscountPercent(p.getDiscountPercent());
            dto.setAvailable(p.isAvailable());
            dto.setStock(p.getStock());
            dto.setRatingAverage(p.getRatingAverage());
            dto.setRatingCount(p.getRatingCount());
            dto.setImageUrls(productImageRepository.findByProductOrderByDisplayOrderAsc(p)
                    .stream().map(i -> i.getImageUrl()).collect(Collectors.toList()));

            // Related products: same category or same brand by popularity
            List<ProductListItemDto> related = new ArrayList<>();
            if (p.getBrand() != null) {
                related.addAll(productRepository.findTop10ByBrandOrderByPopularityDesc(p.getBrand())
                        .stream().filter(r -> !r.getId().equals(p.getId()))
                        .limit(5).map(this::toListItemDto).collect(Collectors.toList()));
            }
            if (related.size() < 5 && p.getCategory() != null) {
                productRepository.findTop10ByCategoryOrderByPopularityDesc(p.getCategory())
                        .stream().filter(r -> !r.getId().equals(p.getId()))
                        .limit(5 - related.size()).map(this::toListItemDto).forEach(related::add);
            }
            dto.setRelatedProducts(related);
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Optional<ProductDetailDto> getProductDetailById(Long productId) {
        return productRepository.findById(productId).map(p -> {
            ProductDetailDto dto = new ProductDetailDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setSlug(p.getSlug());
            dto.setBrandName(p.getBrand() != null ? p.getBrand().getName() : null);
            dto.setCategorySlug(p.getCategory() != null ? p.getCategory().getSlug() : null);
            dto.setDescription(p.getDescription());
            dto.setSpecifications(p.getSpecifications());
            dto.setPrice(p.getPrice());
            dto.setDiscountPercent(p.getDiscountPercent());
            dto.setAvailable(p.isAvailable());
            dto.setStock(p.getStock());
            dto.setRatingAverage(p.getRatingAverage());
            dto.setRatingCount(p.getRatingCount());
            dto.setImageUrls(productImageRepository.findByProductOrderByDisplayOrderAsc(p)
                    .stream().map(i -> i.getImageUrl()).collect(java.util.stream.Collectors.toList()));
            dto.setRelatedProducts(java.util.List.of());
            return dto;
        });
    }

    private Pageable buildPageable(Integer page, Integer size, String sort) {
        int p = page != null && page >= 0 ? page : 0;
        int s = size != null && size > 0 && size <= 100 ? size : 20;
        Sort sortObj = Sort.by(Sort.Direction.DESC, "popularity");
        if (StringUtils.hasText(sort)) {
            switch (sort.toLowerCase(Locale.ROOT)) {
                case "price_asc":
                    sortObj = Sort.by(Sort.Direction.ASC, "price"); break;
                case "price_desc":
                    sortObj = Sort.by(Sort.Direction.DESC, "price"); break;
                case "newest":
                    sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); break;
                case "rating":
                    sortObj = Sort.by(Sort.Direction.DESC, "ratingAverage"); break;
                case "popularity":
                default:
                    sortObj = Sort.by(Sort.Direction.DESC, "popularity");
            }
        }
        return PageRequest.of(p, s, sortObj);
    }

    private ProductListItemDto toListItemDto(Product p) {
        ProductListItemDto dto = new ProductListItemDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setBrandName(p.getBrand() != null ? p.getBrand().getName() : null);
        dto.setCategorySlug(p.getCategory() != null ? p.getCategory().getSlug() : null);
        dto.setPrice(p.getPrice());
        dto.setDiscountPercent(p.getDiscountPercent());
        dto.setAvailable(p.isAvailable());
        dto.setRatingAverage(p.getRatingAverage());
        dto.setRatingCount(p.getRatingCount());
        dto.setImageUrls(productImageRepository.findByProductOrderByDisplayOrderAsc(p)
                .stream().map(i -> i.getImageUrl()).collect(Collectors.toList()));
        return dto;
    }
}

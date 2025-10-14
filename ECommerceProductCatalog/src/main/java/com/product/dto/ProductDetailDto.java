package com.product.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductDetailDto {
    private Long id;
    private String name;
    private String slug;
    private String brandName;
    private String categorySlug;
    private String description;
    private String specifications;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private boolean available;
    private int stock;
    private double ratingAverage;
    private long ratingCount;
    private List<String> imageUrls;
    private List<ProductListItemDto> relatedProducts;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public double getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(double ratingAverage) { this.ratingAverage = ratingAverage; }
    public long getRatingCount() { return ratingCount; }
    public void setRatingCount(long ratingCount) { this.ratingCount = ratingCount; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public List<ProductListItemDto> getRelatedProducts() { return relatedProducts; }
    public void setRelatedProducts(List<ProductListItemDto> relatedProducts) { this.relatedProducts = relatedProducts; }
}

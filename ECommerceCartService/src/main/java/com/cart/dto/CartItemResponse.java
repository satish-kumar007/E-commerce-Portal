package com.cart.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    private Long productId;
    private String name;
    private String slug;
    private String imageUrl;
    private int quantity;
    private BigDecimal unitPriceAtAdd;
    private BigDecimal lineTotal;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPriceAtAdd() { return unitPriceAtAdd; }
    public void setUnitPriceAtAdd(BigDecimal unitPriceAtAdd) { this.unitPriceAtAdd = unitPriceAtAdd; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}

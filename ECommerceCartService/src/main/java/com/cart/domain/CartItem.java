package com.cart.domain;

import java.math.BigDecimal;

public class CartItem {
    private Long productId;
    private String productName;
    private String productSlug;
    private String imageUrl; // primary image
    private int quantity;
    private BigDecimal unitPriceAtAdd; // price snapshot at time of add

    public CartItem() {}

    public CartItem(Long productId, String productName, String productSlug, String imageUrl, int quantity, BigDecimal unitPriceAtAdd) {
        this.productId = productId;
        this.productName = productName;
        this.productSlug = productSlug;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.unitPriceAtAdd = unitPriceAtAdd;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSlug() { return productSlug; }
    public void setProductSlug(String productSlug) { this.productSlug = productSlug; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPriceAtAdd() { return unitPriceAtAdd; }
    public void setUnitPriceAtAdd(BigDecimal unitPriceAtAdd) { this.unitPriceAtAdd = unitPriceAtAdd; }
}

package com.cart.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ProductCatalogClient {

    private final RestTemplate restTemplate;

    // In production this should come from config/discovery (Eureka/Gateway base URL)
    private final String catalogBaseUrl = "http://localhost:8083"; // adjust as needed

    public ProductCatalogClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductInfo getProductById(Long productId) {
        String url = catalogBaseUrl + "/api/catalog/products/id/" + productId;
        ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return null;
        }
        Map<String, Object> m = resp.getBody();
        ProductInfo pi = new ProductInfo();
        pi.setId(((Number)m.get("id")).longValue());
        pi.setName((String) m.get("name"));
        pi.setSlug((String) m.get("slug"));
        Object priceObj = m.get("price");
        if (priceObj instanceof Number n) {
            pi.setPrice(BigDecimal.valueOf(n.doubleValue()));
        } else if (priceObj instanceof String s) {
            pi.setPrice(new BigDecimal(s));
        }
        Object stockObj = m.get("stock");
        if (stockObj instanceof Number n) {
            pi.setStock(n.intValue());
        }
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) m.get("imageUrls");
        if (imageUrls != null && !imageUrls.isEmpty()) {
            pi.setPrimaryImageUrl(imageUrls.get(0));
        }
        Object availableObj = m.get("available");
        pi.setAvailable(availableObj instanceof Boolean b ? b : true);
        return pi;
    }

    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
        private BigDecimal price;
        private int stock;
        private boolean available;
        private String primaryImageUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public String getPrimaryImageUrl() { return primaryImageUrl; }
        public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }
    }
}

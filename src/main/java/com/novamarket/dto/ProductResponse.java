package com.novamarket.dto;

import com.novamarket.model.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        boolean active,
        String categorySlug,
        String categoryName,
        Instant createdAt
) {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.isActive(),
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCreatedAt()
        );
    }
}
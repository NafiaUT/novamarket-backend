package com.novamarket.dto;

import com.novamarket.model.CartItem;
import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        String imageUrl
) {
    public static CartItemResponse fromEntity(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getProduct().getImageUrl()
        );
    }
}
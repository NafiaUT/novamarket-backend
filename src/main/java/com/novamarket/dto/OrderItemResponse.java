package com.novamarket.dto;

import com.novamarket.model.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
    public static OrderItemResponse fromEntity(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
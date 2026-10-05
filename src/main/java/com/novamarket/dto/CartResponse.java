package com.novamarket.dto;

import com.novamarket.model.Cart;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        int totalItems
) {
    public static CartResponse fromEntity(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(CartItemResponse::fromEntity)
                .toList();

        return new CartResponse(
                cart.getId(),
                itemResponses,
                cart.getTotalAmount(),
                cart.getTotalQuantity()
        );
    }
}
package com.novamarket.dto;

import com.novamarket.model.Order;
import com.novamarket.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        List<OrderItemResponse> items,
        Instant createdAt
) {
    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::fromEntity)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}
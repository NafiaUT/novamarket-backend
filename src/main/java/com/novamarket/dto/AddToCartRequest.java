package com.novamarket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull(message = "L'identifiant du produit est obligatoire")
        Long productId,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité minimale est de 1")
        Integer quantity
) {
}
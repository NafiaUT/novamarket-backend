package com.novamarket.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank(message = "Le SKU est obligatoire")
        @Size(max = 50, message = "Le SKU ne doit pas dépasser 50 caractères")
        String sku,

        @NotBlank(message = "Le nom du produit est obligatoire")
        @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
        String name,

        String description,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à zéro")
        BigDecimal price,

        @NotNull(message = "La quantité en stock est obligatoire")
        @Min(value = 0, message = "Le stock ne peut pas être négatif")
        Integer stockQuantity,

        String imageUrl,

        @NotBlank(message = "Le slug de la catégorie est obligatoire")
        String categorySlug
) {
}
package com.novamarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank(message = "L'adresse de livraison est obligatoire")
        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String shippingAddress,

        @NotBlank(message = "Le moyen de paiement est obligatoire")
        String paymentMethod
) {
}
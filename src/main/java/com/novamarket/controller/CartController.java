package com.novamarket.controller;

import com.novamarket.dto.AddToCartRequest;
import com.novamarket.dto.CartResponse;
import com.novamarket.model.User;
import com.novamarket.repository.UserRepository;
import com.novamarket.security.CustomUserDetails;
import com.novamarket.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Panier", description = "Endpoints de gestion du panier d'achat de l'utilisateur connecté")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable"));
    }

    @GetMapping
    @Operation(summary = "Consulter son panier", description = "Retourne le panier de l'utilisateur connecté avec la liste des articles et le montant total calculé.")
    public CartResponse getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return cartService.getCartResponse(getAuthenticatedUser(userDetails));
    }

    @PostMapping("/items")
    @Operation(summary = "Ajouter un produit au panier", description = "Ajoute une quantité d'un produit dans le panier après vérification du stock disponible.")
    public CartResponse addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @RequestBody AddToCartRequest request) {
        return cartService.addToCart(getAuthenticatedUser(userDetails), request);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Modifier la quantité d'un article", description = "Met à jour la quantité d'un article dans le panier (supprime l'article si quantité = 0).")
    public CartResponse updateItemQuantity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Long itemId,
                                           @RequestParam int quantitéy) {
        return cartService.updateItemQuantity(getAuthenticatedUser(userDetails), itemId, quantitéy);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Supprimer un article du panier", description = "Retire un article spécifique du panier.")
    public CartResponse removeItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable Long itemId) {
        return cartService.removeItem(getAuthenticatedUser(userDetails), itemId);
    }

    @DeleteMapping
    @Operation(summary = "Vider le panier", description = "Supprime tous les articles présents dans le panier.")
    public void clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearCart(getAuthenticatedUser(userDetails));
    }
}
package com.novamarket.controller;

import com.novamarket.dto.CheckoutRequest;
import com.novamarket.dto.OrderResponse;
import com.novamarket.model.User;
import com.novamarket.repository.UserRepository;
import com.novamarket.security.CustomUserDetails;
import com.novamarket.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Commandes", description = "Endpoints de passage de commande (Checkout) et historique des commandes")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable"));
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Valider la commande (Checkout)", description = "Opération transactionnelle atomique : vérifie et décrémente les stocks, fige les prix d'achat, vide le panier et crée la commande.")
    public OrderResponse checkout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(getAuthenticatedUser(userDetails), request);
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Historique de mes commandes", description = "Retourne la liste de toutes les commandes passées par l'utilisateur connecté.")
    public List<OrderResponse> getMyOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return orderService.getUserOrders(getAuthenticatedUser(userDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une commande", description = "Retourne les détails et articles d'une commande spécifique de l'utilisateur.")
    public OrderResponse getOrderById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long id) {
        return orderService.getOrderById(getAuthenticatedUser(userDetails), id);
    }
}
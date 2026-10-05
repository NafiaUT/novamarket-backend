package com.novamarket.service;

import com.novamarket.dto.CheckoutRequest;
import com.novamarket.dto.OrderResponse;
import com.novamarket.exception.BadRequestException;
import com.novamarket.exception.InsufficientStockException;
import com.novamarket.exception.ResourceNotFoundException;
import com.novamarket.model.*;
import com.novamarket.repository.OrderRepository;
import com.novamarket.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Impossible de passer commande avec un panier vide.");
        }

        log.info("--> Début du checkout pour l'utilisateur {} ({} articles)", user.getEmail(), cart.getItems().size());

        // 1. Déduction atomique des stocks avec contrôle de concurrence
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Rechargement frais du produit pour vérifier le stock réel
            Product freshProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + product.getId()));

            if (freshProduct.getStockQuantity() < cartItem.getQuantity()) {
                log.warn("Stock insuffisant pour le produit {} (demandé: {}, dispo: {})",
                        freshProduct.getName(), cartItem.getQuantity(), freshProduct.getStockQuantity());
                throw new InsufficientStockException("Stock insuffisant pour '" + freshProduct.getName() +
                        "' (Quantité demandée: " + cartItem.getQuantity() + ", Stock disponible: " + freshProduct.getStockQuantity() + ")");
            }

            // Décrémentation atomique
            freshProduct.setStockQuantity(freshProduct.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(freshProduct);
        }

        // 2. Création de la commande
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderNumber, user, cart.getTotalAmount(), request.shippingAddress());

        // 3. Figeage des éléments de commande (snapshot du prix à l'instant d'achat)
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    order,
                    cartItem.getProduct(),
                    cartItem.getProduct().getName(),
                    cartItem.getProduct().getPrice(),
                    cartItem.getQuantity()
            );
            order.addItem(orderItem);
        }

        // 4. Simulation de paiement validé
        order.setStatus(OrderStatus.PAID);
        Order savedOrder = orderRepository.save(order);

        // 5. Vidage du panier
        cartService.clearCart(user);

        log.info("--> Commande {} validée avec succès pour un montant total de {} EUR", savedOrder.getOrderNumber(), savedOrder.getTotalAmount());
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'identifiant : " + orderId));
        return OrderResponse.fromEntity(order);
    }
}
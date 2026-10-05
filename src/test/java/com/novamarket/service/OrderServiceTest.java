package com.novamarket.service;

import com.novamarket.dto.CheckoutRequest;
import com.novamarket.dto.OrderResponse;
import com.novamarket.exception.BadRequestException;
import com.novamarket.exception.InsufficientStockException;
import com.novamarket.model.*;
import com.novamarket.repository.OrderRepository;
import com.novamarket.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User sampleUser;
    private Product sampleProduct;
    private Cart sampleCart;

    @BeforeEach
    void setUp() {
        sampleUser = new User("client@novamarket.com", "pass", "Sophie", "Martin");
        sampleUser.setId(1L);

        sampleProduct = new Product("SKU-1", "Sony XM5", "Casque", new BigDecimal("350.00"), 5, null, null);
        sampleProduct.setId(10L);

        sampleCart = new Cart(sampleUser);
        sampleCart.setId(100L);
    }

    @Test
    @DisplayName("Devrait valider la commande, décrémenter le stock et vider le panier en cas de succès")
    void checkout_WhenStockAvailable_ShouldCreateOrderAndDecrementStock() {
        // Arrange
        sampleCart.addItem(sampleProduct, 2); // 2 articles demandés (stock dispo: 5)
        when(cartService.getOrCreateCart(sampleUser)).thenReturn(sampleCart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(500L);
            return o;
        });

        CheckoutRequest request = new CheckoutRequest("10 rue de la Paix, Paris", "CB");

        // Act
        OrderResponse response = orderService.checkout(sampleUser, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("700.00")); // 2 * 350.00

        // Vérification de la décrémentation du stock (5 - 2 = 3)
        assertThat(sampleProduct.getStockQuantity()).isEqualTo(3);
        verify(productRepository, times(1)).save(sampleProduct);

        // Vérification du vidage du panier
        verify(cartService, times(1)).clearCart(sampleUser);
    }

    @Test
    @DisplayName("Devrait lever une InsufficientStockException si la quantité demandée dépasse le stock réel")
    void checkout_WhenStockInsufficient_ShouldThrowExceptionAndNotSaveOrder() {
        // Arrange
        sampleCart.addItem(sampleProduct, 10); // 10 demandés alors qu'il n'y en a que 5 en stock
        when(cartService.getOrCreateCart(sampleUser)).thenReturn(sampleCart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));

        CheckoutRequest request = new CheckoutRequest("10 rue de la Paix, Paris", "CB");

        // Act & Assert
        assertThatThrownBy(() -> orderService.checkout(sampleUser, request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Stock insuffisant");

        // La commande ne doit PAS être enregistrée et le panier ne doit PAS être vidé
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("Devrait refuser le checkout si le panier est vide")
    void checkout_WhenCartIsEmpty_ShouldThrowBadRequestException() {
        // Arrange
        when(cartService.getOrCreateCart(sampleUser)).thenReturn(sampleCart); // Panier vide sans articles
        CheckoutRequest request = new CheckoutRequest("10 rue de la Paix, Paris", "CB");

        // Act & Assert
        assertThatThrownBy(() -> orderService.checkout(sampleUser, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("panier vide");
    }
}
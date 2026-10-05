package com.novamarket.service;

import com.novamarket.dto.AddToCartRequest;
import com.novamarket.dto.CartResponse;
import com.novamarket.exception.BadRequestException;
import com.novamarket.exception.ResourceNotFoundException;
import com.novamarket.model.Cart;
import com.novamarket.model.CartItem;
import com.novamarket.model.Product;
import com.novamarket.model.User;
import com.novamarket.repository.CartRepository;
import com.novamarket.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional(readOnly = true)
    public CartResponse getCartResponse(User user) {
        Cart cart = getOrCreateCart(user);
        return CartResponse.fromEntity(cart);
    }

    public CartResponse addToCart(User user, AddToCartRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        if (product.getStockQuantity() < request.quantity()) {
            throw new BadRequestException("Stock insuffisant pour ce produit (disponible : " + product.getStockQuantity() + ")");
        }

        Cart cart = getOrCreateCart(user);
        cart.addItem(product, request.quantity());
        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public CartResponse updateItemQuantity(User user, Long itemId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItem(user, itemId);
        }

        Cart cart = getOrCreateCart(user);
        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable dans le panier"));

        if (itemToUpdate.getProduct().getStockQuantity() < newQuantity) {
            throw new BadRequestException("Stock insuffisant (disponible : " + itemToUpdate.getProduct().getStockQuantity() + ")");
        }

        itemToUpdate.setQuantity(newQuantity);
        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public CartResponse removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.clear();
        cartRepository.save(cart);
    }
}
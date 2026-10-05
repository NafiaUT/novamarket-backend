package com.novamarket.service;

import com.novamarket.dto.PagedResponse;
import com.novamarket.dto.ProductCreateRequest;
import com.novamarket.dto.ProductResponse;
import com.novamarket.exception.BadRequestException;
import com.novamarket.exception.ResourceNotFoundException;
import com.novamarket.model.Category;
import com.novamarket.model.Product;
import com.novamarket.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Category sampleCategory;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Ordinateurs", "ordinateurs", "Laptops", null);
        sampleCategory.setId(10L);

        sampleProduct = new Product("MAC-M3", "MacBook Pro", "Apple Silicon", new BigDecimal("2999.00"), 10, null, sampleCategory);
        sampleProduct.setId(1L);
    }

    @Test
    @DisplayName("Devrait retourner un produit quand l'ID existe et le produit est actif")
    void getProductById_WhenProductExists_ShouldReturnProductResponse() {
        // Arrange
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));

        // Act
        ProductResponse response = productService.getProductById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("MAC-M3");
        assertThat(response.name()).isEqualTo("MacBook Pro");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("2999.00"));
        verify(productRepository, times(1)).findByIdAndActiveTrue(1L);
    }

    @Test
    @DisplayName("Devrait lever une ResourceNotFoundException quand le produit n'existe pas")
    void getProductById_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException si le SKU existe déjà à la création")
    void createProduct_WhenSkuAlreadyExists_ShouldThrowBadRequestException() {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest(
                "DUPLICATE-SKU", "Nouveau Produit", "Desc", new BigDecimal("99.00"), 5, null, "ordinateurs"
        );
        when(productRepository.existsBySku("DUPLICATE-SKU")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DUPLICATE-SKU");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait lister les produits actifs avec pagination")
    void getActiveProducts_ShouldReturnPagedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findByActiveTrue(pageable)).thenReturn(page);

        // Act
        PagedResponse<ProductResponse> result = productService.getActiveProducts(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }
}
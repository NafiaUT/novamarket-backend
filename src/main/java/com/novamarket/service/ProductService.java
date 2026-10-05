package com.novamarket.service;

import com.novamarket.config.CacheConfig;
import com.novamarket.dto.PagedResponse;
import com.novamarket.dto.ProductCreateRequest;
import com.novamarket.dto.ProductResponse;
import com.novamarket.exception.BadRequestException;
import com.novamarket.exception.ResourceNotFoundException;
import com.novamarket.model.Category;
import com.novamarket.model.Product;
import com.novamarket.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public PagedResponse<ProductResponse> getActiveProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return PagedResponse.from(productPage.map(ProductResponse::fromEntity));
    }

    public PagedResponse<ProductResponse> getProductsByCategory(String categorySlug, Pageable pageable) {
        categoryService.getCategoryBySlug(categorySlug); // Valide que la categorie existe
        Page<Product> productPage = productRepository.findByCategorySlugAndActiveTrue(categorySlug, pageable);
        return PagedResponse.from(productPage.map(ProductResponse::fromEntity));
    }

    public PagedResponse<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable);
        return PagedResponse.from(productPage.map(ProductResponse::fromEntity));
    }

    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'identifiant : " + id));
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, allEntries = true)
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new BadRequestException("Un produit avec le SKU '" + request.sku() + "' existe déjà");
        }

        Category category = categoryService.getCategoryBySlug(request.categorySlug());

        Product product = new Product(
                request.sku(),
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.imageUrl(),
                category
        );

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }
}
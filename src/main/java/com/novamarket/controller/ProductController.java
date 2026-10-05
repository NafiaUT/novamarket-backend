package com.novamarket.controller;

import com.novamarket.dto.PagedResponse;
import com.novamarket.dto.ProductCreateRequest;
import com.novamarket.dto.ProductResponse;
import com.novamarket.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produits", description = "Endpoints de consultation, recherche paginée et création de produits")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lister les produits paginés", description = "Permet de récupérer les produits avec pagination et tri (par exemple: page=0, size=10, sort=price,asc).")
    public PagedResponse<ProductResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return productService.getActiveProducts(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un produit par ID", description = "Retourne les informations complètes d'un produit (résultat mis en cache).")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/category/{slug}")
    @Operation(summary = "Lister les produits d'une catégorie", description = "Retourne les produits filtrés par le slug de leur catégorie.")
    public PagedResponse<ProductResponse> getProductsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productService.getProductsByCategory(slug, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des produits", description = "Recherche textuelle insensible à la casse sur le nom des produits.")
    public PagedResponse<ProductResponse> searchProducts(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productService.searchProducts(keyword, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau produit", description = "Valide les données entrantes, crée le produit et invalide le cache.")
    public ProductResponse createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return productService.createProduct(request);
    }

    private Sort parseSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by("id").descending();
        }
        String property = sort[0];
        String direction = sort.length > 1 ? sort[1] : "asc";
        return direction.equalsIgnoreCase("desc")
                ? Sort.by(property).descending()
                : Sort.by(property).ascending();
    }
}
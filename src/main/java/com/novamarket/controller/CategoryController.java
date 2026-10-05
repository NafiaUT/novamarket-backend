package com.novamarket.controller;

import com.novamarket.dto.CategoryDto;
import com.novamarket.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Catégories", description = "Endpoints de gestion et consultation des rayons du catalogue")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les catégories", description = "Retourne la liste complète des catégories de produits (résultat mis en cache).")
    public List<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Consulter une catégorie par son slug", description = "Retourne le détail d'une catégorie identifiée par son slug.")
    public CategoryDto getCategoryBySlug(@PathVariable String slug) {
        return CategoryDto.fromEntity(categoryService.getCategoryBySlug(slug));
    }
}
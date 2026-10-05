package com.novamarket.service;

import com.novamarket.config.CacheConfig;
import com.novamarket.dto.CategoryDto;
import com.novamarket.exception.ResourceNotFoundException;
import com.novamarket.model.Category;
import com.novamarket.repository.CategoryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(CacheConfig.CACHE_CATEGORIES)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec le slug : " + slug));
    }
}
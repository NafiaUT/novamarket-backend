package com.novamarket.repository;

import com.novamarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.slug = :categorySlug AND p.active = true")
    Page<Product> findByCategorySlugAndActiveTrue(@Param("categorySlug") String categorySlug, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword, Pageable pageable);

    boolean existsBySku(String sku);
}
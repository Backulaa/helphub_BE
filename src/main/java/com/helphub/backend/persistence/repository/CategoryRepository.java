package com.helphub.backend.persistence.repository;

import com.helphub.backend.persistence.entity.Category;
import com.helphub.backend.persistence.repository.projection.CategorySupportRequestCountProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByCodeIgnoreCase(String code);

    List<Category> findAllByIsActiveTrueOrderByCreatedAtDesc();

    List<Category> findAllByOrderByCreatedAtDesc();

    long countByIsActiveTrue();

    @Query("""
            SELECT c.id AS categoryId,
                   c.name AS categoryName,
                   COUNT(sr.id) AS supportRequestCount
            FROM Category c
            LEFT JOIN SupportRequest sr ON sr.category = c
            GROUP BY c.id, c.name
            ORDER BY c.name ASC
            """)
    List<CategorySupportRequestCountProjection> countSupportRequestsByCategory();
}
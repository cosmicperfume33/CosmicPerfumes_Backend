package com.example.ForgeX.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ForgeX.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    Optional<Category> findBySlugIgnoreCase(String slug);

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(String categoryName, Long categoryId);

    boolean existsBySlugIgnoreCaseAndCategoryIdNot(String slug, Long categoryId);

    @Query("SELECT COALESCE(MAX(c.sortOrder), -1) FROM Category c")
    int maxSortOrder();

    @Query("SELECT p.category.categoryId, COUNT(p) FROM Product p "
            + "WHERE p.category.categoryId IN :ids GROUP BY p.category.categoryId")
    List<Object[]> countProductsByCategoryIds(@Param("ids") List<Long> ids);
}
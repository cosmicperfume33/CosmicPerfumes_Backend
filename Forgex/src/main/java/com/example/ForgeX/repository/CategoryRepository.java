package com.example.ForgeX.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ForgeX.model.Category;

@Repository 
public interface  CategoryRepository extends JpaRepository<Category,Long> {

    Category findByCategoryName(String categoryName);

    Optional<Category> findBySlugIgnoreCase(String category);
    
}

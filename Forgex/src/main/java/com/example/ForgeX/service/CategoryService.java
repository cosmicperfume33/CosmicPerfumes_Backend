package com.example.ForgeX.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.dto.CategoryDTO;
import com.example.ForgeX.dto.CategoryResponse;

import io.jsonwebtoken.io.IOException;

public interface CategoryService {
    public CategoryDTO createCategory(CategoryDTO request);

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO updateCategories(Long catgoryId, CategoryDTO categoryDTO);
    
    String deleteCategory(Long categoryId);
    
    CategoryDTO updateIconImage(Long productId,MultipartFile file) throws IOException, java.io.IOException;

}

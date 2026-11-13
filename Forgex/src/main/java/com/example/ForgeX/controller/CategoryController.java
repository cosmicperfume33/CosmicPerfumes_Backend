package com.example.ForgeX.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.config.AppConstants;
import com.example.ForgeX.dto.CategoryDTO;
import com.example.ForgeX.dto.CategoryResponse;
import com.example.ForgeX.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO category = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/category/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategories(@PathVariable Long categoryId,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updateCategory = categoryService.updateCategories(categoryId, categoryDTO);
        return new ResponseEntity<>(updateCategory, HttpStatus.OK);
    }

    @DeleteMapping("/admin/category/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        String deleted = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    @PutMapping("/admin/categoryIcon/{categoryId}")
    public ResponseEntity<CategoryDTO> updateIconImage(@PathVariable Long categoryId,
            @RequestParam("file") MultipartFile file) throws IOException {
        CategoryDTO responDto = categoryService.updateIconImage(categoryId, file);
        return new ResponseEntity<>(responDto, HttpStatus.OK);
    }
}
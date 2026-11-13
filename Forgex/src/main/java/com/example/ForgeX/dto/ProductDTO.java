package com.example.ForgeX.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.ForgeX.model.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must be 100 characters or fewer")
    private String name;

    @Size(max = 120, message = "Slug must be 120 characters or fewer")
    private String slug;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String category;

    private Long productTypeId;
    private String productTypeName;
    private String productTypeSlug;

    private Gender gender;
    private String fragranceFamily;
    private String material;

    private Integer quantity;

    @PositiveOrZero(message = "MRP cannot be negative")
    private Long mrp;

    @PositiveOrZero(message = "Price cannot be negative")
    private Long price;

    private Integer discount;

    @PositiveOrZero(message = "USD MRP cannot be negative")
    private Long mrpUsd;

    @PositiveOrZero(message = "USD price cannot be negative")
    private Long priceUsd;

    private Integer discountUsd;

    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    private String image;
    private List<ProductImageDTO> images = new ArrayList<>();

    private Boolean active = true;
}
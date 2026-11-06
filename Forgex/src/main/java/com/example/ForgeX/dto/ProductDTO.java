package com.example.ForgeX.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.ForgeX.model.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;
    private String name;
    private String slug;
    private String description;

    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String category;

    private Gender gender;
    private String fragranceFamily;
    private String material;

    private Integer quantity;

    private Long mrp;
    private Long price;
    private Integer discount;

    private Long mrpUsd;
    private Long priceUsd;
    private Integer discountUsd;

    private Integer stock;
    private String image;
    private List<ProductImageDTO> images = new ArrayList<>();

    private Boolean active = true;
}
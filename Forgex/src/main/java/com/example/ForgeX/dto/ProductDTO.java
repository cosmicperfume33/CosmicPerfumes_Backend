package com.example.ForgeX.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.ForgeX.model.Category;
import com.example.ForgeX.model.FragranceFamily;
import com.example.ForgeX.model.Gender;
import com.example.ForgeX.model.Material;
import com.example.ForgeX.model.ProductImage;

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

    private Category category;
    private Gender gender;
    private FragranceFamily fragranceFamily;
    private Material material;

    private Integer quantity; 

    private Long mrp; 
    private Long price;
    private Integer discount; 

    private Long mrpUsd; // original price, in cents
    private Long priceUsd; // selling price, in cents
    private Integer discountUsd; // calculated, never sent by client

    private Integer stock;
    private String image;
    private List<ProductImageDTO> images = new ArrayList<>();

    public List<ProductImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ProductImageDTO> images) {
        this.images = images;
    }

    private Boolean active = true;
}
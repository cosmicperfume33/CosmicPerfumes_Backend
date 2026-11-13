package com.example.ForgeX.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;
    private String slug;
    private String iconImage;
}

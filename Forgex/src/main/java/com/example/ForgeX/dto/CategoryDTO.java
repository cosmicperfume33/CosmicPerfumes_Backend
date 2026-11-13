package com.example.ForgeX.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    private Long categoryId;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be 100 characters or fewer")
    @JsonProperty("categoryName")
    @JsonAlias("name")
    private String categoryName;

    @Size(max = 120, message = "Slug must be 120 characters or fewer")
    private String slug;

    @Size(max = 400, message = "Description must be 400 characters or fewer")
    private String description;

    @JsonProperty("iconImage")
    @JsonAlias({ "imageUrl", "icon", "image" })
    private String iconImage;

    private String theme;

    private Integer sortOrder;

    private Boolean active;

    private Long productCount;
}
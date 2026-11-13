package com.example.ForgeX.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductTypeDTO {

    private Long productTypeId;

    @NotBlank(message = "Type name is required")
    @Size(max = 100, message = "Type name must be 100 characters or fewer")
    @JsonProperty("productTypeName")
    @JsonAlias({ "typeName", "name" })
    private String typeName;

    @NotBlank(message = "Slug is required")
    @Size(max = 120, message = "Slug must be 120 characters or fewer")
    private String slug;

    private Integer displayOrder;

    @NotNull(message = "Choose a category for this type")
    private Long categoryId;

    private String categoryName;

    private String categorySlug;

    private Boolean active;

    private Long productCount;
}
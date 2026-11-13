package com.example.ForgeX.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "category",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_slug", columnNames = "slug"),
        indexes = @Index(name = "idx_category_order", columnList = "sort_order"))
public class Category {

    public enum Theme {
        LIGHT,
        DARK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be 100 characters or fewer")
    @Column(nullable = false, length = 100)
    private String categoryName;

    @NotBlank(message = "Slug is required")
    @Size(max = 120, message = "Slug must be 120 characters or fewer")
    @Column(nullable = false, length = 120)
    private String slug;

    @Size(max = 400, message = "Description must be 400 characters or fewer")
    @Column(length = 400)
    private String description;

    @Column(name = "icon_image", length = 500)
    private String iconImage;

    @Column(name = "icon_image_public_id", length = 255)
    private String iconImagePublicId;

    @Column(length = 10)
    private String theme = Theme.LIGHT.name();

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Category(String categoryName, String slug) {
        this.categoryName = categoryName;
        this.slug = slug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category other)) {
            return false;
        }
        return categoryId != null && categoryId.equals(other.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(categoryId);
    }

    @Override
    public String toString() {
        return "Category{categoryId=" + categoryId + ", categoryName=" + categoryName + "}";
    }
}
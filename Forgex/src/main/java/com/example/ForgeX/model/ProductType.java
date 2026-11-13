package com.example.ForgeX.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_type",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_type_slug", columnNames = "slug"),
        indexes = @Index(name = "idx_product_type_category", columnList = "category_id"))
@Getter
@Setter
@NoArgsConstructor
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productTypeId;

    @NotBlank(message = "Type name is required")
    @Size(max = 100, message = "Type name must be 100 characters or fewer")
    @Column(nullable = false, length = 100)
    private String typeName;

    @NotBlank(message = "Slug is required")
    @Size(max = 120, message = "Slug must be 120 characters or fewer")
    @Column(nullable = false, length = 120)
    private String slug;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    public ProductType(String typeName, String slug, Category category) {
        this.typeName = typeName;
        this.slug = slug;
        this.category = category;
    }

    public Long getCategoryId() {
        return category == null ? null : category.getCategoryId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductType other)) {
            return false;
        }
        return productTypeId != null && productTypeId.equals(other.productTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(productTypeId);
    }

    @Override
    public String toString() {
        return "ProductType{productTypeId=" + productTypeId + ", typeName=" + typeName + "}";
    }
}
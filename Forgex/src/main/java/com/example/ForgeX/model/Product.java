package com.example.ForgeX.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_type", columnList = "product_type_id"),
                @Index(name = "idx_product_active", columnList = "active")
        })
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(name = "fragrance_family", length = 50)
    private String fragranceFamily;

    @Column(length = 50)
    private String material;

    private Long mrp;
    private Long price;

    @Column(name = "mrp_usd")
    private Long mrpUsd;

    @Column(name = "price_usd")
    private Long priceUsd;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 500)
    private String image;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, imageId ASC")
    @BatchSize(size = 100)
    private List<ProductImage> images = new ArrayList<>();

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images.clear();
        if (images != null) {
            images.forEach(this::addImage);
        }
    }

    public void addImage(ProductImage productImage) {
        images.add(productImage);
        productImage.setProduct(this);
    }

    public void removeImage(ProductImage productImage) {
        images.remove(productImage);
        productImage.setProduct(null);
    }

    public Category typeCategory() {
        return productType == null ? null : productType.getCategory();
    }

    public String taxonomyConflict() {
        Category owner = typeCategory();
        if (productType == null || owner == null || category == null) {
            return null;
        }
        if (Objects.equals(owner.getCategoryId(), category.getCategoryId())) {
            return null;
        }
        return "'" + productType.getTypeName() + "' belongs to " + owner.getCategoryName()
                + ", not " + category.getCategoryName();
    }

    public Integer getDiscount() {
        return percentOff(mrp, price);
    }

    public Integer getDiscountUsd() {
        return percentOff(mrpUsd, priceUsd);
    }

    private static Integer percentOff(Long mrp, Long price) {
        if (mrp == null || price == null || mrp <= 0 || price >= mrp) {
            return 0;
        }
        return (int) ((mrp - price) * 100 / mrp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product other)) {
            return false;
        }
        return productId != null && productId.equals(other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(productId);
    }

    @Override
    public String toString() {
        return "Product{productId=" + productId + ", name=" + name + ", slug=" + slug + "}";
    }
}
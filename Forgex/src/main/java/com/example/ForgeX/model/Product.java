package com.example.ForgeX.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.example.ForgeX.service.FileService.UploadedImage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 2000)
    private String description;

    

    @Enumerated(EnumType.STRING)
    private Gender gender;

    
    private String fragranceFamily;
    private String material;


    private Long mrp;
    private Long price;

    private Long mrpUsd;
    private Long priceUsd;

    private Integer stock;
    private String image;
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

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

    public Integer getDiscount() {
        if (mrp == null || price == null || mrp == 0)
            return 0;
        return (int) ((mrp - price) * 100 / mrp);
    }

    public Integer getDiscountUsd() {
        if (mrpUsd == null || priceUsd == null || mrpUsd == 0)
            return 0;
        return (int) ((mrpUsd - priceUsd) * 100 / mrpUsd);
    }

    public void addImage(ProductImage productImage) {
        images.add(productImage);
        productImage.setProduct(this);
    }

    public void removeImage(ProductImage productImage) {
        images.remove(productImage);
        productImage.setProduct(null);
    }
}
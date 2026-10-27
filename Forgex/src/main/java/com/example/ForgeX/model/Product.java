package com.example.ForgeX.model;

import java.util.ArrayList;
import java.util.List;

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
    private Category category;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private FragranceFamily fragranceFamily;

    @Enumerated(EnumType.STRING)
    private Material material;

    private Integer quantity; 

    private Long mrp; 
    private Long price;

    private Integer stock;
    private String image;
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, imageId ASC")
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

    public void addImage(ProductImage productImage) {
        images.add(productImage);
        productImage.setProduct(this);
    }

    public void removeImage(ProductImage productImage) {
        images.remove(productImage);
        productImage.setProduct(null);
    }
}
package com.example.ForgeX.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerImageDTO {

    private Long imageId;
    private Integer sortOrder;
    private String imageUrl;
    private String mobileImageUrl;
    private String altText;
    private Boolean decorative;
    private String linkHref;
    private String linkLabel;
    private String eyebrow;
    private String heading;
    private String subheading;
}

package com.example.ForgeX.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {

    private Long bannerId;
    private String placement;
    private String internalName;
    private String eyebrow;
    private String heading;
    private String subheading;
    private String ctaLabel;
    private String ctaHref;
    private String theme;
    private Integer sortOrder;
    private Boolean active;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private List<BannerImageDTO> images = new ArrayList<>();
    private Boolean live;
    private Boolean singleSlot;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

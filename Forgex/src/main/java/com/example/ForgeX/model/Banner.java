package com.example.ForgeX.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "banners", indexes = {
        @Index(name = "idx_banner_placement_order", columnList = "placement, sort_order"),
        @Index(name = "idx_banner_active", columnList = "active")
})
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "placement", nullable = false, length = 40)
    private BannerPlacement placement;

    @Column(name = "internal_name", length = 120)
    private String internalName;

    @Column(name = "eyebrow", length = 80)
    private String eyebrow;

    @Column(name = "heading", length = 200)
    private String heading;

    @Column(name = "subheading", length = 300)
    private String subheading;

    @Column(name = "cta_label", length = 40)
    private String ctaLabel;

    @Column(name = "cta_href", length = 500)
    private String ctaHref;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 10)
    private BannerTheme theme = BannerTheme.DARK;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @OneToMany(mappedBy = "banner", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<BannerImage> images = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addImage(BannerImage image) {
        image.setBanner(this);
        this.images.add(image);
    }

    public void removeImage(BannerImage image) {
        this.images.remove(image);
        image.setBanner(null);
    }

    public Long getBannerId() {
        return bannerId;
    }

    public void setBannerId(Long bannerId) {
        this.bannerId = bannerId;
    }

    public BannerPlacement getPlacement() {
        return placement;
    }

    public void setPlacement(BannerPlacement placement) {
        this.placement = placement;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getEyebrow() {
        return eyebrow;
    }

    public void setEyebrow(String eyebrow) {
        this.eyebrow = eyebrow;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getSubheading() {
        return subheading;
    }

    public void setSubheading(String subheading) {
        this.subheading = subheading;
    }

    public String getCtaLabel() {
        return ctaLabel;
    }

    public void setCtaLabel(String ctaLabel) {
        this.ctaLabel = ctaLabel;
    }

    public String getCtaHref() {
        return ctaHref;
    }

    public void setCtaHref(String ctaHref) {
        this.ctaHref = ctaHref;
    }

    public BannerTheme getTheme() {
        return theme;
    }

    public void setTheme(BannerTheme theme) {
        this.theme = theme;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public List<BannerImage> getImages() {
        return images;
    }

    public void setImages(List<BannerImage> images) {
        this.images = images;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

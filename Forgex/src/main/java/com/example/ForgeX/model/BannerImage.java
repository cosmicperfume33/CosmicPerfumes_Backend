package com.example.ForgeX.model;

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

@Entity
@Table(name = "banner_images", indexes = {
        @Index(name = "idx_banner_image_order", columnList = "banner_id, sort_order")
})
public class BannerImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banner_id", nullable = false)
    private Banner banner;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Column(name = "mobile_image_url", length = 500)
    private String mobileImageUrl;

    @Column(name = "mobile_public_id", length = 255)
    private String mobilePublicId;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(name = "decorative", nullable = false)
    private boolean decorative = false;

    @Column(name = "link_href", length = 500)
    private String linkHref;

    @Column(name = "link_label", length = 40)
    private String linkLabel;

    @Column(name = "eyebrow", length = 80)
    private String eyebrow;

    @Column(name = "heading", length = 200)
    private String heading;

    @Column(name = "subheading", length = 300)
    private String subheading;

    public BannerImage() {
    }

    public BannerImage(String imageUrl, String publicId, int sortOrder) {
        this.imageUrl = imageUrl;
        this.publicId = publicId;
        this.sortOrder = sortOrder;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getMobileImageUrl() {
        return mobileImageUrl;
    }

    public void setMobileImageUrl(String mobileImageUrl) {
        this.mobileImageUrl = mobileImageUrl;
    }

    public String getMobilePublicId() {
        return mobilePublicId;
    }

    public void setMobilePublicId(String mobilePublicId) {
        this.mobilePublicId = mobilePublicId;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public boolean isDecorative() {
        return decorative;
    }

    public void setDecorative(boolean decorative) {
        this.decorative = decorative;
    }

    public String getLinkHref() {
        return linkHref;
    }

    public void setLinkHref(String linkHref) {
        this.linkHref = linkHref;
    }

    public String getLinkLabel() {
        return linkLabel;
    }

    public void setLinkLabel(String linkLabel) {
        this.linkLabel = linkLabel;
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
}

package com.example.ForgeX.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.dto.APIResponse;
import com.example.ForgeX.dto.BannerDTO;
import com.example.ForgeX.dto.BannerImageDTO;
import com.example.ForgeX.dto.BannerImageUploadResponse;
import com.example.ForgeX.dto.BannerReorderDTO;
import com.example.ForgeX.service.BannerService;

@RestController
@RequestMapping("/api")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/public/banners")
    public ResponseEntity<Map<String, List<BannerDTO>>> getLiveBanners() {
        return new ResponseEntity<>(bannerService.getLiveBanners(), HttpStatus.OK);
    }

    @GetMapping("/public/banners/{placement}")
    public ResponseEntity<List<BannerDTO>> getLiveBannersByPlacement(@PathVariable String placement) {
        return new ResponseEntity<>(bannerService.getLiveBanners(placement), HttpStatus.OK);
    }

    @GetMapping("/admin/banners")
    public ResponseEntity<List<BannerDTO>> getBanners(
            @RequestParam(name = "placement", required = false) String placement) {
        return new ResponseEntity<>(bannerService.getBanners(placement), HttpStatus.OK);
    }

    @GetMapping("/admin/banners/{bannerId}")
    public ResponseEntity<BannerDTO> getBanner(@PathVariable Long bannerId) {
        return new ResponseEntity<>(bannerService.getBanner(bannerId), HttpStatus.OK);
    }

    @PostMapping("/admin/banners")
    public ResponseEntity<BannerDTO> addBanner(@RequestBody BannerDTO bannerDTO) {
        return new ResponseEntity<>(bannerService.createBanner(bannerDTO), HttpStatus.CREATED);
    }

    @PutMapping("/admin/banners/{bannerId}")
    public ResponseEntity<BannerDTO> updateBanner(@PathVariable Long bannerId,
            @RequestBody BannerDTO bannerDTO) {
        return new ResponseEntity<>(bannerService.updateBanner(bannerId, bannerDTO), HttpStatus.OK);
    }

    @PatchMapping("/admin/banners/{bannerId}/active")
    public ResponseEntity<BannerDTO> setActive(@PathVariable Long bannerId,
            @RequestParam("active") boolean active) {
        return new ResponseEntity<>(bannerService.setActive(bannerId, active), HttpStatus.OK);
    }

    @PutMapping("/admin/banners/reorder")
    public ResponseEntity<List<BannerDTO>> reorderBanners(@RequestBody BannerReorderDTO request) {
        return new ResponseEntity<>(bannerService.reorderBanners(request), HttpStatus.OK);
    }

    @DeleteMapping("/admin/banners/{bannerId}")
    public ResponseEntity<APIResponse> deleteBanner(@PathVariable Long bannerId) {
        String message = bannerService.deleteBanner(bannerId);
        return new ResponseEntity<>(new APIResponse(message, true), HttpStatus.OK);
    }

    @PostMapping("/admin/banners/{bannerId}/images")
    public ResponseEntity<BannerImageUploadResponse> addBannerImages(@PathVariable Long bannerId,
            @RequestParam("images") List<MultipartFile> images) {
        return new ResponseEntity<>(bannerService.addImages(bannerId, images), HttpStatus.CREATED);
    }

    @PutMapping("/admin/banners/{bannerId}/images/{imageId}/file")
    public ResponseEntity<BannerDTO> replaceBannerImage(@PathVariable Long bannerId,
            @PathVariable Long imageId,
            @RequestParam("image") MultipartFile image) throws IOException {
        return new ResponseEntity<>(bannerService.replaceImage(bannerId, imageId, image), HttpStatus.OK);
    }

    @PutMapping("/admin/banners/{bannerId}/images/{imageId}/mobile")
    public ResponseEntity<BannerDTO> setMobileImage(@PathVariable Long bannerId,
            @PathVariable Long imageId,
            @RequestParam("image") MultipartFile image) throws IOException {
        return new ResponseEntity<>(bannerService.setMobileImage(bannerId, imageId, image), HttpStatus.OK);
    }

    @DeleteMapping("/admin/banners/{bannerId}/images/{imageId}/mobile")
    public ResponseEntity<BannerDTO> removeMobileImage(@PathVariable Long bannerId,
            @PathVariable Long imageId) {
        return new ResponseEntity<>(bannerService.removeMobileImage(bannerId, imageId), HttpStatus.OK);
    }

    @PutMapping("/admin/banners/{bannerId}/images/{imageId}")
    public ResponseEntity<BannerDTO> updateBannerImage(@PathVariable Long bannerId,
            @PathVariable Long imageId,
            @RequestBody BannerImageDTO imageDTO) {
        return new ResponseEntity<>(bannerService.updateImage(bannerId, imageId, imageDTO), HttpStatus.OK);
    }

    @PutMapping("/admin/banners/{bannerId}/images/reorder")
    public ResponseEntity<BannerDTO> reorderBannerImages(@PathVariable Long bannerId,
            @RequestBody BannerReorderDTO request) {
        return new ResponseEntity<>(bannerService.reorderImages(bannerId, request), HttpStatus.OK);
    }

    @DeleteMapping("/admin/banners/{bannerId}/images/{imageId}")
    public ResponseEntity<BannerDTO> deleteBannerImage(@PathVariable Long bannerId,
            @PathVariable Long imageId) {
        return new ResponseEntity<>(bannerService.deleteImage(bannerId, imageId), HttpStatus.OK);
    }
}

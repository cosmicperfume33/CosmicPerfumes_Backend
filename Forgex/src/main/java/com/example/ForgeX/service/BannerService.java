package com.example.ForgeX.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.dto.BannerDTO;
import com.example.ForgeX.dto.BannerImageDTO;
import com.example.ForgeX.dto.BannerImageUploadResponse;
import com.example.ForgeX.dto.BannerReorderDTO;

public interface BannerService {

    Map<String, List<BannerDTO>> getLiveBanners();

    List<BannerDTO> getLiveBanners(String placement);

    List<BannerDTO> getBanners(String placement);

    BannerDTO getBanner(Long bannerId);

    BannerDTO createBanner(BannerDTO bannerDTO);

    BannerDTO updateBanner(Long bannerId, BannerDTO bannerDTO);

    BannerDTO setActive(Long bannerId, boolean active);

    List<BannerDTO> reorderBanners(BannerReorderDTO request);

    String deleteBanner(Long bannerId);

    BannerImageUploadResponse addImages(Long bannerId, List<MultipartFile> files);

    BannerDTO replaceImage(Long bannerId, Long imageId, MultipartFile file) throws IOException;

    BannerDTO setMobileImage(Long bannerId, Long imageId, MultipartFile file) throws IOException;

    BannerDTO removeMobileImage(Long bannerId, Long imageId);

    BannerDTO updateImage(Long bannerId, Long imageId, BannerImageDTO imageDTO);

    BannerDTO reorderImages(Long bannerId, BannerReorderDTO request);

    BannerDTO deleteImage(Long bannerId, Long imageId);
}

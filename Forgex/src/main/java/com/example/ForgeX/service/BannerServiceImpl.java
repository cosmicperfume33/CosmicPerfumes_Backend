package com.example.ForgeX.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.dto.BannerDTO;
import com.example.ForgeX.dto.BannerImageDTO;
import com.example.ForgeX.dto.BannerImageUploadResponse;
import com.example.ForgeX.dto.BannerReorderDTO;
import com.example.ForgeX.dto.BannerUploadResultDTO;
import com.example.ForgeX.exceptions.APIException;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Banner;
import com.example.ForgeX.model.BannerImage;
import com.example.ForgeX.model.BannerPlacement;
import com.example.ForgeX.model.BannerTheme;
import com.example.ForgeX.repository.BannerRepository;
import com.example.ForgeX.service.FileService.UploadedImage;
import com.example.ForgeX.util.BannerLinks;

@Service
public class BannerServiceImpl implements BannerService {

    private static final int MAX_FILES_PER_UPLOAD = 10;
    private static final int MAX_IMAGES_PER_BANNER = 12;
    private static final int MAX_LABEL_LENGTH = 40;

    private final BannerRepository bannerRepository;
    private final FileService fileService;
    private final String folder;

    public BannerServiceImpl(BannerRepository bannerRepository,
            FileService fileService,
            @Value("${banner.image.folder:cosmiclife/banners}") String folder) {
        this.bannerRepository = bannerRepository;
        this.fileService = fileService;
        this.folder = folder;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<BannerDTO>> getLiveBanners() {
        List<Banner> live = liveBanners(null);
        Map<String, List<BannerDTO>> grouped = new LinkedHashMap<>();
        for (BannerPlacement placement : BannerPlacement.values()) {
            grouped.put(placement.name(), new ArrayList<>());
        }
        for (Banner banner : live) {
            grouped.get(banner.getPlacement().name()).add(toDTO(banner, true));
        }
        return grouped;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerDTO> getLiveBanners(String placement) {
        BannerPlacement target = BannerPlacement.from(placement);
        return liveBanners(target).stream().map(banner -> toDTO(banner, true)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerDTO> getBanners(String placement) {
        List<Banner> banners = (placement == null || placement.isBlank())
                ? bannerRepository.findAllWithImages()
                : bannerRepository.findByPlacementWithImages(BannerPlacement.from(placement));
        LocalDateTime now = LocalDateTime.now();
        return banners.stream().map(banner -> toDTO(banner, isLive(banner, now))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BannerDTO getBanner(Long bannerId) {
        Banner banner = requireBanner(bannerId);
        return toDTO(banner, isLive(banner, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public BannerDTO createBanner(BannerDTO bannerDTO) {
        Banner banner = new Banner();
        banner.setPlacement(BannerPlacement.from(bannerDTO.getPlacement()));
        applyBannerFields(banner, bannerDTO);

        Integer requested = bannerDTO.getSortOrder();
        banner.setSortOrder(requested != null ? requested : bannerRepository.maxSortOrder(banner.getPlacement()) + 1);
        banner.setActive(bannerDTO.getActive() == null || bannerDTO.getActive());

        return save(banner);
    }

    @Override
    @Transactional
    public BannerDTO updateBanner(Long bannerId, BannerDTO bannerDTO) {
        Banner banner = requireBanner(bannerId);
        BannerPlacement current = banner.getPlacement();

        if (bannerDTO.getPlacement() != null && !bannerDTO.getPlacement().isBlank()) {
            BannerPlacement moved = BannerPlacement.from(bannerDTO.getPlacement());
            if (moved != current) {
                banner.setPlacement(moved);
                banner.setSortOrder(bannerRepository.maxSortOrder(moved) + 1);
            }
        }

        applyBannerFields(banner, bannerDTO);

        if (bannerDTO.getSortOrder() != null && banner.getPlacement() == current) {
            banner.setSortOrder(bannerDTO.getSortOrder());
        }
        if (bannerDTO.getActive() != null) {
            if (bannerDTO.getActive()) {
                requireDisplayable(banner);
            }
            banner.setActive(bannerDTO.getActive());
        }

        return save(banner);
    }

    @Override
    @Transactional
    public BannerDTO setActive(Long bannerId, boolean active) {
        Banner banner = requireBanner(bannerId);
        if (active) {
            requireDisplayable(banner);
        }
        banner.setActive(active);
        return save(banner);
    }

    @Override
    @Transactional
    public List<BannerDTO> reorderBanners(BannerReorderDTO request) {
        if (request == null) {
            throw new APIException("Send the placement and the full list of banners in their new order.");
        }
        BannerPlacement placement = BannerPlacement.from(request.getPlacement());
        List<Banner> banners = bannerRepository.findByPlacementWithImages(placement);
        applyOrder(banners, request.getIds(), Banner::getBannerId, Banner::setSortOrder, "banner");
        bannerRepository.saveAll(banners);

        LocalDateTime now = LocalDateTime.now();
        return banners.stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(banner -> toDTO(banner, isLive(banner, now)))
                .toList();
    }

    @Override
    @Transactional
    public String deleteBanner(Long bannerId) {
        Banner banner = requireBanner(bannerId);
        List<String> publicIds = new ArrayList<>();
        for (BannerImage image : banner.getImages()) {
            publicIds.add(image.getPublicId());
            publicIds.add(image.getMobilePublicId());
        }

        bannerRepository.delete(banner);
        bannerRepository.flush();
        publicIds.forEach(fileService::deleteImage);

        return "Banner deleted successfully";
    }

    @Override
    @Transactional
    public BannerImageUploadResponse addImages(Long bannerId, List<MultipartFile> files) {
        Banner banner = requireBanner(bannerId);

        if (files == null || files.isEmpty()) {
            throw new APIException("Choose at least one image to upload.");
        }
        if (files.size() > MAX_FILES_PER_UPLOAD) {
            throw new APIException("Upload up to " + MAX_FILES_PER_UPLOAD + " images at a time.");
        }
        if (banner.getImages().size() + files.size() > MAX_IMAGES_PER_BANNER) {
            throw new APIException("A banner holds up to " + MAX_IMAGES_PER_BANNER
                    + " images. This one already has " + banner.getImages().size() + ".");
        }

        int next = nextImageOrder(banner);
        List<BannerUploadResultDTO> results = new ArrayList<>();
        List<BannerImage> added = new ArrayList<>();
        String firstFailure = null;

        for (MultipartFile file : files) {
            String name = (file == null || file.getOriginalFilename() == null) ? "file" : file.getOriginalFilename();
            try {
                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("Image must not be empty");
                }
                UploadedImage uploaded = fileService.uploadImages(folder, List.of(file)).get(0);
                BannerImage image = new BannerImage(uploaded.url(), uploaded.publicId(), next++);
                banner.addImage(image);
                added.add(image);
                results.add(new BannerUploadResultDTO(name, true, null, null));
            } catch (IOException | RuntimeException e) {
                String reason = e.getMessage() == null ? "Upload failed" : e.getMessage();
                if (firstFailure == null) {
                    firstFailure = reason;
                }
                results.add(new BannerUploadResultDTO(name, false, reason, null));
            }
        }

        if (added.isEmpty()) {
            throw new APIException(firstFailure == null ? "No image could be uploaded." : firstFailure);
        }

        BannerDTO saved = save(banner);
        for (int i = 0, slot = 0; i < results.size(); i++) {
            if (results.get(i).isUploaded()) {
                results.get(i).setImageId(added.get(slot++).getImageId());
            }
        }

        return new BannerImageUploadResponse(saved, results);
    }

    @Override
    @Transactional
    public BannerDTO replaceImage(Long bannerId, Long imageId, MultipartFile file) throws IOException {
        Banner banner = requireBanner(bannerId);
        BannerImage image = requireImage(banner, imageId);

        UploadedImage uploaded = uploadOne(file);
        String previous = image.getPublicId();
        image.setImageUrl(uploaded.url());
        image.setPublicId(uploaded.publicId());

        BannerDTO saved = save(banner);
        fileService.deleteImage(previous);
        return saved;
    }

    @Override
    @Transactional
    public BannerDTO setMobileImage(Long bannerId, Long imageId, MultipartFile file) throws IOException {
        Banner banner = requireBanner(bannerId);
        BannerImage image = requireImage(banner, imageId);

        UploadedImage uploaded = uploadOne(file);
        String previous = image.getMobilePublicId();
        image.setMobileImageUrl(uploaded.url());
        image.setMobilePublicId(uploaded.publicId());

        BannerDTO saved = save(banner);
        fileService.deleteImage(previous);
        return saved;
    }

    @Override
    @Transactional
    public BannerDTO removeMobileImage(Long bannerId, Long imageId) {
        Banner banner = requireBanner(bannerId);
        BannerImage image = requireImage(banner, imageId);

        String previous = image.getMobilePublicId();
        image.setMobileImageUrl(null);
        image.setMobilePublicId(null);

        BannerDTO saved = save(banner);
        fileService.deleteImage(previous);
        return saved;
    }

    @Override
    @Transactional
    public BannerDTO updateImage(Long bannerId, Long imageId, BannerImageDTO imageDTO) {
        Banner banner = requireBanner(bannerId);
        BannerImage image = requireImage(banner, imageId);

        if (imageDTO == null) {
            throw new APIException("Nothing to update.");
        }

        image.setAltText(trimToNull(imageDTO.getAltText()));
        image.setDecorative(imageDTO.getDecorative() != null && imageDTO.getDecorative());
        image.setLinkHref(BannerLinks.normalise(imageDTO.getLinkHref()));
        image.setLinkLabel(trimToNull(imageDTO.getLinkLabel()));
        image.setEyebrow(trimToNull(imageDTO.getEyebrow()));
        image.setHeading(trimToNull(imageDTO.getHeading()));
        image.setSubheading(trimToNull(imageDTO.getSubheading()));

        requireLabelHasLink(image.getLinkHref(), image.getLinkLabel());
        if (image.isDecorative() && image.getAltText() != null) {
            image.setAltText(null);
        }

        return save(banner);
    }

    @Override
    @Transactional
    public BannerDTO reorderImages(Long bannerId, BannerReorderDTO request) {
        Banner banner = requireBanner(bannerId);
        if (request == null) {
            throw new APIException("Send the full list of images in their new order.");
        }
        applyOrder(banner.getImages(), request.getIds(), BannerImage::getImageId, BannerImage::setSortOrder, "image");
        banner.getImages().sort((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()));
        return save(banner);
    }

    @Override
    @Transactional
    public BannerDTO deleteImage(Long bannerId, Long imageId) {
        Banner banner = requireBanner(bannerId);
        BannerImage image = requireImage(banner, imageId);

        String desktop = image.getPublicId();
        String mobile = image.getMobilePublicId();

        banner.removeImage(image);
        resequence(banner);

        if (banner.getImages().isEmpty() && banner.isActive()) {
            banner.setActive(false);
        }

        BannerDTO saved = save(banner);
        fileService.deleteImage(desktop);
        fileService.deleteImage(mobile);
        return saved;
    }

    private List<Banner> liveBanners(BannerPlacement placement) {
        LocalDateTime now = LocalDateTime.now();
        List<Banner> live = new ArrayList<>();
        Set<BannerPlacement> filled = new HashSet<>();

        for (Banner banner : bannerRepository.findLiveWithImages(now)) {
            if (banner.getImages().isEmpty()) {
                continue;
            }
            if (placement != null && banner.getPlacement() != placement) {
                continue;
            }
            if (banner.getPlacement().isSingleSlot() && !filled.add(banner.getPlacement())) {
                continue;
            }
            live.add(banner);
        }
        return live;
    }

    private void applyBannerFields(Banner banner, BannerDTO dto) {
        banner.setInternalName(trimToNull(dto.getInternalName()));
        banner.setEyebrow(trimToNull(dto.getEyebrow()));
        banner.setHeading(trimToNull(dto.getHeading()));
        banner.setSubheading(trimToNull(dto.getSubheading()));
        banner.setCtaLabel(trimToNull(dto.getCtaLabel()));
        banner.setCtaHref(BannerLinks.normalise(dto.getCtaHref()));
        banner.setTheme(BannerTheme.from(dto.getTheme(), banner.getTheme()));
        banner.setStartsAt(dto.getStartsAt());
        banner.setEndsAt(dto.getEndsAt());

        requireCopy(banner);
        requireLabelLength(banner.getCtaLabel());
        requireLabelHasLink(banner.getCtaHref(), banner.getCtaLabel());
        requireSaneSchedule(banner);
    }

    private void requireCopy(Banner banner) {
        if (isBlank(banner.getEyebrow()) && isBlank(banner.getHeading()) && isBlank(banner.getSubheading())) {
            throw new APIException("Add a headline, or at least an eyebrow or subheading.");
        }
    }

    private void requireLabelHasLink(String href, String label) {
        if (!isBlank(label) && isBlank(href)) {
            throw new APIException("Add where the button should go.");
        }
        requireLabelLength(label);
    }

    private void requireLabelLength(String label) {
        if (label != null && label.length() > MAX_LABEL_LENGTH) {
            throw new APIException("Keep the button text to " + MAX_LABEL_LENGTH + " characters or fewer.");
        }
    }

    private void requireSaneSchedule(Banner banner) {
        if (banner.getStartsAt() != null && banner.getEndsAt() != null
                && banner.getEndsAt().isBefore(banner.getStartsAt())) {
            throw new APIException("The end date comes before the start date.");
        }
    }

    private void requireDisplayable(Banner banner) {
        if (banner.getImages().isEmpty()) {
            throw new APIException("Add an image before switching this on.");
        }
        for (BannerImage image : banner.getImages()) {
            if (!image.isDecorative() && isBlank(image.getAltText())) {
                throw new APIException("Describe every image, or mark it decorative, before the banner goes live.");
            }
        }
    }

    private <T> void applyOrder(List<T> items, List<Long> ids, java.util.function.Function<T, Long> idOf,
            java.util.function.BiConsumer<T, Integer> setOrder, String what) {
        if (ids == null || ids.isEmpty()) {
            throw new APIException("Send every " + what + " in its new order.");
        }
        Set<Long> given = new HashSet<>(ids);
        if (given.size() != ids.size()) {
            throw new APIException("The same " + what + " appears twice in the order.");
        }
        Set<Long> known = new HashSet<>();
        items.forEach(item -> known.add(idOf.apply(item)));
        if (!given.equals(known)) {
            throw new APIException("The order must name every " + what + " exactly once.");
        }
        for (T item : items) {
            setOrder.accept(item, ids.indexOf(idOf.apply(item)));
        }
    }

    private void resequence(Banner banner) {
        List<BannerImage> images = banner.getImages();
        images.sort((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()));
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setSortOrder(i);
        }
    }

    private int nextImageOrder(Banner banner) {
        return banner.getImages().stream().mapToInt(BannerImage::getSortOrder).max().orElse(-1) + 1;
    }

    private UploadedImage uploadOne(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new APIException("Choose an image to upload.");
        }
        try {
            return fileService.uploadImages(folder, List.of(file)).get(0);
        } catch (IllegalArgumentException e) {
            throw new APIException(e.getMessage());
        }
    }

    private Banner requireBanner(Long bannerId) {
        if (bannerId == null) {
            throw new APIException("Which banner?");
        }
        return bannerRepository.findByIdWithImages(bannerId)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", "bannerId", bannerId));
    }

    private BannerImage requireImage(Banner banner, Long imageId) {
        Optional<BannerImage> found = banner.getImages().stream()
                .filter(image -> image.getImageId().equals(imageId))
                .findFirst();
        return found.orElseThrow(() -> new ResourceNotFoundException("Banner image", "imageId", imageId));
    }

    private BannerDTO save(Banner banner) {
        try {
            Banner saved = bannerRepository.saveAndFlush(banner);
            return toDTO(saved, isLive(saved, LocalDateTime.now()));
        } catch (OptimisticLockingFailureException e) {
            throw new APIException("Someone else saved changes to this banner. Reload to see them.");
        }
    }

    private boolean isLive(Banner banner, LocalDateTime now) {
        return banner.isActive()
                && !banner.getImages().isEmpty()
                && (banner.getStartsAt() == null || !banner.getStartsAt().isAfter(now))
                && (banner.getEndsAt() == null || !banner.getEndsAt().isBefore(now));
    }

    private BannerDTO toDTO(Banner banner, boolean live) {
        BannerDTO dto = new BannerDTO();
        dto.setBannerId(banner.getBannerId());
        dto.setPlacement(banner.getPlacement().name());
        dto.setInternalName(banner.getInternalName());
        dto.setEyebrow(banner.getEyebrow());
        dto.setHeading(banner.getHeading());
        dto.setSubheading(banner.getSubheading());
        dto.setCtaLabel(banner.getCtaLabel());
        dto.setCtaHref(banner.getCtaHref());
        dto.setTheme(banner.getTheme().name());
        dto.setSortOrder(banner.getSortOrder());
        dto.setActive(banner.isActive());
        dto.setStartsAt(banner.getStartsAt());
        dto.setEndsAt(banner.getEndsAt());
        dto.setLive(live);
        dto.setSingleSlot(banner.getPlacement().isSingleSlot());
        dto.setVersion(banner.getVersion());
        dto.setCreatedAt(banner.getCreatedAt());
        dto.setUpdatedAt(banner.getUpdatedAt());

        List<BannerImageDTO> images = new ArrayList<>();
        for (BannerImage image : banner.getImages()) {
            images.add(toDTO(image, banner));
        }
        dto.setImages(images);
        return dto;
    }

    private BannerImageDTO toDTO(BannerImage image, Banner banner) {
        BannerImageDTO dto = new BannerImageDTO();
        dto.setImageId(image.getImageId());
        dto.setSortOrder(image.getSortOrder());
        dto.setImageUrl(image.getImageUrl());
        dto.setMobileImageUrl(image.getMobileImageUrl() != null ? image.getMobileImageUrl() : image.getImageUrl());
        dto.setAltText(image.isDecorative() ? "" : image.getAltText());
        dto.setDecorative(image.isDecorative());
        dto.setLinkHref(image.getLinkHref() != null ? image.getLinkHref() : banner.getCtaHref());
        dto.setLinkLabel(image.getLinkLabel() != null ? image.getLinkLabel() : banner.getCtaLabel());
        dto.setEyebrow(image.getEyebrow() != null ? image.getEyebrow() : banner.getEyebrow());
        dto.setHeading(image.getHeading() != null ? image.getHeading() : banner.getHeading());
        dto.setSubheading(image.getSubheading() != null ? image.getSubheading() : banner.getSubheading());
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

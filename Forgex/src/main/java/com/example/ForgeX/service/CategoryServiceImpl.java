package com.example.ForgeX.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.dto.CategoryDTO;
import com.example.ForgeX.dto.CategoryResponse;
import com.example.ForgeX.exceptions.APIException;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Category;
import com.example.ForgeX.repository.CategoryRepository;
import com.example.ForgeX.repository.ProductRepository;
import com.example.ForgeX.service.FileService.UploadedImage;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final Set<String> SORTABLE =
            Set.of("categoryId", "categoryName", "slug", "sortOrder");

    private static final int MAX_PAGE_SIZE = 200;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO request) {
        String name = require(request.getCategoryName(), "Category name is required");
        String slug = resolveSlug(request.getSlug(), name);

        if (categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new APIException("A category named '" + name + "' already exists");
        }
        if (categoryRepository.existsBySlugIgnoreCase(slug)) {
            throw new APIException("The web address '" + slug + "' is already used by another category");
        }

        Category category = new Category();
        category.setCategoryName(name);
        category.setSlug(slug);
        category.setDescription(trimToNull(request.getDescription()));
        category.setIconImage(trimToNull(request.getIconImage()));
        category.setTheme(parseTheme(request.getTheme()));
        category.setSortOrder(request.getSortOrder() != null
                ? Math.max(request.getSortOrder(), 0)
                : categoryRepository.maxSortOrder() + 1);
        category.setActive(request.getActive() == null || request.getActive());

        return toDTO(categoryRepository.save(category), 0L);
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Page<Category> page =
                categoryRepository.findAll(buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategories(Long categoryId, CategoryDTO categoryDTO) {
        Category categoryDB = find(categoryId);

        String name = require(categoryDTO.getCategoryName(), "Category name is required");
        String slug = resolveSlug(categoryDTO.getSlug(), name);

        if (categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot(name, categoryId)) {
            throw new APIException("A category named '" + name + "' already exists");
        }
        if (categoryRepository.existsBySlugIgnoreCaseAndCategoryIdNot(slug, categoryId)) {
            throw new APIException("The web address '" + slug + "' is already used by another category");
        }

        categoryDB.setCategoryName(name);
        categoryDB.setSlug(slug);
        categoryDB.setDescription(trimToNull(categoryDTO.getDescription()));

        String icon = trimToNull(categoryDTO.getIconImage());
        if (icon != null) {
            if (!icon.equals(categoryDB.getIconImage())) {
                String previous = categoryDB.getIconImagePublicId();
                categoryDB.setIconImage(icon);
                categoryDB.setIconImagePublicId(null);
                if (previous != null && !previous.isBlank()) {
                    fileService.deleteImage(previous);
                }
            }
        }

        if (categoryDTO.getTheme() != null) {
            categoryDB.setTheme(parseTheme(categoryDTO.getTheme()));
        }
        if (categoryDTO.getSortOrder() != null) {
            categoryDB.setSortOrder(Math.max(categoryDTO.getSortOrder(), 0));
        }
        if (categoryDTO.getActive() != null) {
            categoryDB.setActive(categoryDTO.getActive());
        }

        return toDTO(categoryDB, countFor(categoryId));
    }

    @Override
    @Transactional
    public String deleteCategory(Long categoryId) {
        Category categoryDB = find(categoryId);

        if (productRepository.existsByCategory_CategoryId(categoryId)) {
            throw new APIException("This category still has products in it. "
                    + "Move or delete them first, or hide the category instead.");
        }

        String publicId = categoryDB.getIconImagePublicId();
        categoryRepository.delete(categoryDB);
        if (publicId != null && !publicId.isBlank()) {
            fileService.deleteImage(publicId);
        }

        return "Category deleted Successfully!!";
    }

    @Override
    @Transactional
    public CategoryDTO updateIconImage(Long categoryId, MultipartFile file) throws IOException {
        Category categoryFromDb = find(categoryId);

        if (file == null || file.isEmpty()) {
            throw new APIException("Choose an image to upload.");
        }

        String oldPublicId = categoryFromDb.getIconImagePublicId();

        List<UploadedImage> uploadedImages = fileService.uploadImages(path, List.of(file));
        UploadedImage uploadedImage = uploadedImages.get(0);

        categoryFromDb.setIconImage(uploadedImage.url());
        categoryFromDb.setIconImagePublicId(uploadedImage.publicId());

        CategoryDTO dto = toDTO(categoryFromDb, countFor(categoryId));

        if (oldPublicId != null && !oldPublicId.isBlank()
                && !oldPublicId.equals(uploadedImage.publicId())) {
            fileService.deleteImage(oldPublicId);
        }

        return dto;
    }

    private Category find(Long categoryId) {
        if (categoryId == null) {
            throw new APIException("A category id is required");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private PageRequest buildPage(Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {

        int page = pageNumber == null ? 0 : pageNumber;
        int size = pageSize == null ? 50 : pageSize;

        if (page < 0) {
            throw new APIException("pageNumber cannot be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new APIException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        String field = "sortOrder";
        if (sortBy != null && !sortBy.isBlank()) {
            field = SORTABLE.stream()
                    .filter(f -> f.equalsIgnoreCase(sortBy.trim()))
                    .findFirst()
                    .orElseThrow(() -> new APIException("Cannot sort by '" + sortBy.trim()
                            + "'. Allowed: " + String.join(", ", SORTABLE)));
        }

        Sort sort = (sortOrder == null || "asc".equalsIgnoreCase(sortOrder.trim()))
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();

        return PageRequest.of(page, size, sort);
    }

    private CategoryResponse buildResponse(Page<Category> page) {
        List<Long> ids = page.getContent().stream().map(Category::getCategoryId).toList();
        Map<Long, Long> counts = countFor(ids);

        List<CategoryDTO> categoryDTOs = page.getContent().stream()
                .map(c -> toDTO(c, counts.getOrDefault(c.getCategoryId(), 0L)))
                .toList();

        CategoryResponse response = new CategoryResponse();
        response.setContent(categoryDTOs);
        response.setLastPage(page.isLast());
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }

    private Map<Long, Long> countFor(List<Long> ids) {
        Map<Long, Long> counts = new HashMap<>();
        if (ids.isEmpty()) {
            return counts;
        }
        for (Object[] row : categoryRepository.countProductsByCategoryIds(ids)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private long countFor(Long categoryId) {
        return countFor(List.of(categoryId)).getOrDefault(categoryId, 0L);
    }

    private CategoryDTO toDTO(Category category, long productCount) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setIconImage(category.getIconImage());
        dto.setTheme(category.getTheme());
        dto.setSortOrder(category.getSortOrder());
        dto.setActive(category.getActive());
        dto.setProductCount(productCount);
        return dto;
    }

    private static String parseTheme(String raw) {
        if (raw == null || raw.isBlank()) {
            return Category.Theme.LIGHT.name();
        }
        try {
            return Category.Theme.valueOf(raw.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException e) {
            throw new APIException("Theme must be LIGHT or DARK.");
        }
    }

    private static String require(String raw, String message) {
        if (raw == null || raw.isBlank()) {
            throw new APIException(message);
        }
        return raw.trim();
    }

    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String resolveSlug(String rawSlug, String fallback) {
        String raw = (rawSlug == null || rawSlug.isBlank()) ? fallback : rawSlug;
        String slug = slugify(raw);
        if (slug.isEmpty()) {
            throw new APIException("Category name must contain at least one letter or number");
        }
        return slug;
    }

    private static String slugify(String raw) {
        if (raw == null) {
            return "";
        }
        String slug = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return slug.length() > 120 ? slug.substring(0, 120).replaceAll("-+$", "") : slug;
    }
}
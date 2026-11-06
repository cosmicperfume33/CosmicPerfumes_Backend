package com.example.ForgeX.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.dto.ProductDTO;
import com.example.ForgeX.dto.ProductImageDTO;
import com.example.ForgeX.dto.ProductResponse;
import com.example.ForgeX.exceptions.APIException;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Category;
import com.example.ForgeX.model.Gender;
import com.example.ForgeX.model.Product;
import com.example.ForgeX.model.ProductImage;
import com.example.ForgeX.repository.CategoryRepository;
import com.example.ForgeX.repository.ProductRepository;
import com.example.ForgeX.service.FileService.UploadedImage;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    private static final int MAX_IMAGES = 8;

    @Value("${project.image}")
    private String path;

    @Override
    @Transactional
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = requireCategory(categoryId);

        String name = require(productDTO.getName(), "Product name is required");
        String slug = resolveSlug(productDTO, name);

        if (productRepository.existsByNameIgnoreCase(name))
            throw new APIException("A product named '" + name + "' already exists");
        if (productRepository.existsBySlugIgnoreCase(slug))
            throw new APIException("The web address '" + slug + "' is already used by another product");

        Product product = modelMapper.map(productDTO, Product.class);
        product.setProductId(null);
        product.setName(name);
        product.setSlug(slug);
        product.setCategory(category);
        applyCategoryFields(product, productDTO, category);

        if (product.getActive() == null)
            product.setActive(true);
        if (product.getStock() == null)
            product.setStock(0);

        replaceImages(product, productDTO);
        ensurePrimaryImage(product);

        return toDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDB = findProduct(productId);

        String name = require(productDTO.getName(), "Product name is required");
        String slug = resolveSlug(productDTO, name);

        if (productRepository.existsByNameIgnoreCaseAndProductIdNot(name, productId))
            throw new APIException("A product named '" + name + "' already exists");
        if (productRepository.existsBySlugIgnoreCaseAndProductIdNot(slug, productId))
            throw new APIException("The web address '" + slug + "' is already used by another product");

        Category category = productDTO.getCategoryId() == null
                ? productFromDB.getCategory()
                : requireCategory(productDTO.getCategoryId());
        if (category == null)
            throw new APIException("Choose a category for this product");

        productFromDB.setName(name);
        productFromDB.setSlug(slug);
        productFromDB.setDescription(productDTO.getDescription());
        productFromDB.setCategory(category);
        productFromDB.setMrp(productDTO.getMrp());
        productFromDB.setPrice(productDTO.getPrice());
        productFromDB.setMrpUsd(productDTO.getMrpUsd());
        productFromDB.setPriceUsd(productDTO.getPriceUsd());

        if (productDTO.getStock() != null)
            productFromDB.setStock(productDTO.getStock());
        if (productDTO.getActive() != null)
            productFromDB.setActive(productDTO.getActive());

        applyCategoryFields(productFromDB, productDTO, category);
        ensurePrimaryImage(productFromDB);

        return toDTO(productRepository.save(productFromDB));
    }

    @Override
    @Transactional
    public String deleteProduct(Long productId) {
        Product productFromDB = findProduct(productId);
        productRepository.delete(productFromDB);
        return "Product deleted successfully";
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Page<Product> page = productRepository.findAll(
                buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    public ProductResponse getProductByKeyWord(String keyword, Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Page<Product> page = productRepository.findByNameLikeIgnoreCase(
                "%" + keyword + "%",
                buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    public ProductResponse sortProductByCategory(String category, Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Category categoryFromDB = categoryRepository.findAll().stream()
                .filter(c -> matches(c, category))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Category", "category", category));

        Page<Product> page = productRepository.findByCategory(
                categoryFromDB,
                buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    public ProductResponse sortProductByGender(String gender, Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Gender genderEnum;
        try {
            genderEnum = Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new APIException("Unknown gender '" + gender + "'");
        }
        Page<Product> page = productRepository.findByGender(
                genderEnum,
                buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    @Transactional
    public ProductDTO updateProductImage(Long productId, List<MultipartFile> files) throws IOException {
        Product product = findProduct(productId);

        if (files == null || files.isEmpty())
            throw new APIException("Choose at least one image to upload.");

        List<ProductImage> current = product.getImages();

        if (current.size() + files.size() > MAX_IMAGES)
            throw new APIException("A product can have at most " + MAX_IMAGES
                    + " images; this one already has " + current.size() + ".");

        List<UploadedImage> uploaded = fileService.uploadImages(path, files);

        int next = current.stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        for (UploadedImage image : uploaded) {
            product.addImage(new ProductImage(image.url(), image.publicId(), next++));
        }

        if (product.getImage() == null || product.getImage().isBlank())
            product.setImage(uploaded.get(0).url());

        return toDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO deleteProductImage(Long productId, Long imageId) {
        Product product = findProduct(productId);

        ProductImage image = product.getImages().stream()
                .filter(i -> i.getImageId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "imageId", imageId));

        boolean wasPrimary = image.getImageUrl().equals(product.getImage());
        String publicId = image.getPublicId();

        product.removeImage(image);

        List<ProductImage> remaining = resequence(product);

        if (wasPrimary)
            product.setImage(remaining.isEmpty() ? null : remaining.get(0).getImageUrl());

        Product saved = productRepository.save(product);

        fileService.deleteImage(publicId);

        return toDTO(saved);
    }

    @Override
    @Transactional
    public ProductDTO setPrimaryProductImage(Long productId, Long imageId) {
        Product product = findProduct(productId);

        ProductImage image = product.getImages().stream()
                .filter(i -> i.getImageId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "imageId", imageId));

        image.setSortOrder(-1);
        resequence(product);
        product.setImage(image.getImageUrl());

        return toDTO(productRepository.save(product));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    private Category requireCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private void applyCategoryFields(Product product, ProductDTO productDTO, Category category) {
        String code = legacyCode(category);
        if ("HANDICRAFT".equals(code)) {
            product.setMaterial(productDTO.getMaterial());
            product.setGender(null);
            product.setFragranceFamily(null);
        } else if ("ATTAR".equals(code) || "PERFUME".equals(code)) {
            product.setMaterial(null);
            product.setGender(productDTO.getGender());
            product.setFragranceFamily(productDTO.getFragranceFamily());
        } else {
            product.setMaterial(null);
            product.setGender(null);
            product.setFragranceFamily(null);
        }
    }

    private static String legacyCode(Category category) {
        String name = category == null ? null : category.getCategoryName();
        if (name == null)
            return null;
        String key = name.trim().toLowerCase();
        if (key.startsWith("handicraft"))
            return "HANDICRAFT";
        if (key.startsWith("attar"))
            return "ATTAR";
        if (key.startsWith("perfume"))
            return "PERFUME";
        return null;
    }

    private void replaceImages(Product product, ProductDTO productDTO) {
        product.getImages().clear();
        if (productDTO.getImages() == null)
            return;
        int order = 0;
        for (ProductImageDTO imageDTO : productDTO.getImages()) {
            if (order >= MAX_IMAGES)
                break;
            if (imageDTO.getImageUrl() == null || imageDTO.getImageUrl().isBlank())
                continue;
            ProductImage productImage = new ProductImage();
            productImage.setImageUrl(imageDTO.getImageUrl().trim());
            productImage.setSortOrder(order++);
            product.addImage(productImage);
        }
    }

    private void ensurePrimaryImage(Product product) {
        boolean missing = product.getImage() == null || product.getImage().isBlank();
        if (missing && !product.getImages().isEmpty())
            product.setImage(product.getImages().get(0).getImageUrl());
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        Category category = product.getCategory();
        if (category != null) {
            dto.setCategoryId(category.getCategoryId());
            dto.setCategoryName(category.getCategoryName());
            dto.setCategorySlug(slugify(category.getCategoryName()));
            dto.setCategory(legacyCode(category));
        }
        return dto;
    }

    private ProductResponse buildResponse(Page<Product> page) {
        List<ProductDTO> dtos = page.getContent().stream()
                .map(this::toDTO)
                .toList();

        return new ProductResponse(
                dtos,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    private PageRequest buildPage(Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private List<ProductImage> resequence(Product product) {
        List<ProductImage> ordered = product.getImages();

        ordered.sort(
                Comparator.comparingInt(ProductImage::getSortOrder)
                        .thenComparing(
                                ProductImage::getImageId,
                                Comparator.nullsLast(Comparator.naturalOrder())));

        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setSortOrder(i);
        }

        return ordered;
    }

    private static boolean matches(Category category, String wanted) {
        if (category == null || category.getCategoryName() == null || wanted == null)
            return false;
        String name = category.getCategoryName().trim();
        return name.equalsIgnoreCase(wanted.trim()) || slugify(name).equals(slugify(wanted));
    }

    private String resolveSlug(ProductDTO productDTO, String name) {
        String raw = productDTO.getSlug() == null || productDTO.getSlug().isBlank()
                ? name
                : productDTO.getSlug();
        String slug = slugify(raw);
        if (slug.isEmpty())
            throw new APIException("Product name must contain at least one letter or number");
        return slug;
    }

    private static String require(String raw, String message) {
        if (raw == null || raw.isBlank())
            throw new APIException(message);
        return raw.trim();
    }

    private static String slugify(String raw) {
        if (raw == null)
            return "";
        String slug = raw.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return slug.length() > 120 ? slug.substring(0, 120).replaceAll("-+$", "") : slug;
    }
}
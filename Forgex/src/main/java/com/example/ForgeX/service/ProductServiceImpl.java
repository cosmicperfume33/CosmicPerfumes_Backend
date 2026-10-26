package com.example.ForgeX.service;

import java.io.IOException;
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
import java.util.Comparator;
import java.util.List;

import com.example.ForgeX.dto.ProductDTO;
import com.example.ForgeX.dto.ProductImageDTO;
import com.example.ForgeX.dto.ProductResponse;
import com.example.ForgeX.exceptions.APIException;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Category;
import com.example.ForgeX.model.Gender;
import com.example.ForgeX.model.Product;
import com.example.ForgeX.model.ProductImage;
import com.example.ForgeX.repository.ProductRepository;
import com.example.ForgeX.service.FileService.UploadedImage;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    @Transactional
    public ProductDTO addProduct(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);
        product.setProductId(null);
        if (product.getActive() == null)
            product.setActive(true);

        product.getImages().clear();

        if (productDTO.getImages() != null) {
            int order = 0;
            for (ProductImageDTO imageDTO : productDTO.getImages()) {
                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageDTO.getImageUrl());
                productImage.setSortOrder(order++);
                product.addImage(productImage);
            }
        }
        if ((product.getImage() == null || product.getImage().isBlank())
                && product.getImages() != null
                && !product.getImages().isEmpty()) {

            product.setImage(product.getImages().get(0).getImageUrl());
        }

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
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
        Category categoryEnum = Category.valueOf(category.toUpperCase());
        Page<Product> page = productRepository.findByCategory(
                categoryEnum,
                buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    public ProductResponse sortProductByGender(String gender, Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Gender genderEnum = Gender.valueOf(gender.toUpperCase());
        Page<Product> page = productRepository.findByGender(
                genderEnum,
                buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDB = findProduct(productId);

        productFromDB.setName(productDTO.getName());
        productFromDB.setSlug(productDTO.getSlug());
        productFromDB.setDescription(productDTO.getDescription());
        productFromDB.setCategory(productDTO.getCategory());
        productFromDB.setGender(productDTO.getGender());
        productFromDB.setFragranceFamily(productDTO.getFragranceFamily());
        productFromDB.setMrp(productDTO.getMrp());
        productFromDB.setPrice(productDTO.getPrice());
        productFromDB.setStock(productDTO.getStock());

        if (productDTO.getActive() != null) {
            productFromDB.setActive(productDTO.getActive());
        }

        Product savedProduct = productRepository.save(productFromDB);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

   

    @Override
    @Transactional
    public String deleteProduct(Long productId) {
        Product productFromDB = findProduct(productId);
        productRepository.delete(productFromDB);
        return "Product deleted successfully";
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    private PageRequest buildPage(Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private ProductResponse buildResponse(Page<Product> page) {
        List<ProductDTO> dtos = page.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return new ProductResponse(
                dtos,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

private static final int MAX_IMAGES = 8;

@Override
@Transactional
public ProductDTO updateProductImage(
        Long productId,
        List<MultipartFile> files
) throws IOException {

    Product product = findProduct(productId);

    if (files == null || files.isEmpty()) {
        throw new APIException("Choose at least one image to upload.");
    }

    List<ProductImage> current = product.getImages();

    if (current.size() + files.size() > MAX_IMAGES) {
        throw new APIException(
                "A product can have at most " + MAX_IMAGES
                        + " images; this one already has " + current.size() + "."
        );
    }

    List<UploadedImage> uploaded = fileService.uploadImages(path, files);

    int next = current.stream()
            .mapToInt(ProductImage::getSortOrder)
            .max()
            .orElse(-1) + 1;

    for (UploadedImage image : uploaded) {
        product.addImage(
                new ProductImage(image.url(), image.publicId(), next++)
        );
    }

    if (product.getImage() == null || product.getImage().isBlank()) {
        product.setImage(uploaded.get(0).url());
    }

    return modelMapper.map(
            productRepository.save(product),
            ProductDTO.class
    );
}

@Override
@Transactional
public ProductDTO deleteProductImage(Long productId, Long imageId) {
    Product product = findProduct(productId);

    ProductImage image = product.getImages().stream()
            .filter(i -> i.getImageId().equals(imageId))
            .findFirst()
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "ProductImage", "imageId", imageId
                    )
            );

    boolean wasPrimary = image.getImageUrl().equals(product.getImage());
    String publicId = image.getPublicId();

    product.removeImage(image);

    List<ProductImage> remaining = resequence(product);

    if (wasPrimary) {
        product.setImage(
                remaining.isEmpty() ? null : remaining.get(0).getImageUrl()
        );
    }

    Product saved = productRepository.save(product);

    fileService.deleteImage(publicId);

    return modelMapper.map(saved, ProductDTO.class);
}

@Override
@Transactional
public ProductDTO setPrimaryProductImage(Long productId, Long imageId) {
    Product product = findProduct(productId);

    ProductImage image = product.getImages().stream()
            .filter(i -> i.getImageId().equals(imageId))
            .findFirst()
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "ProductImage", "imageId", imageId
                    )
            );

    image.setSortOrder(-1);
    resequence(product);
    product.setImage(image.getImageUrl());

    return modelMapper.map(
            productRepository.save(product),
            ProductDTO.class
    );
}

private List<ProductImage> resequence(Product product) {
    List<ProductImage> ordered = product.getImages();

    ordered.sort(
            Comparator.comparingInt(ProductImage::getSortOrder)
                    .thenComparing(
                            ProductImage::getImageId,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
    );

    for (int i = 0; i < ordered.size(); i++) {
        ordered.get(i).setSortOrder(i);
    }

    return ordered;
}
}
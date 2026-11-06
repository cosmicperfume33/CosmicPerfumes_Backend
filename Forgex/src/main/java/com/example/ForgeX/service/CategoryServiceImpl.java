package com.example.ForgeX.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Audited;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ForgeX.dto.CategoryDTO;
import com.example.ForgeX.dto.CategoryResponse;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Category;
import com.example.ForgeX.repository.CategoryRepository;
import com.example.ForgeX.service.FileService.UploadedImage;

import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public CategoryDTO createCategory(CategoryDTO request) {
        Category category = modelMapper.map(request, Category.class);

        Category categoryFromDB = categoryRepository.findByCategoryName(category.getCategoryName());

        if (categoryFromDB != null) {
            throw new RuntimeException("Category with name: " + category.getCategoryName() + " is already exist...");

        }
        Category saved = categoryRepository.save(category);
        return modelMapper.map(saved, CategoryDTO.class);

    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Page<Category> page = categoryRepository.findAll(buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildResponse(page);
    }

    @Override
    public CategoryDTO updateCategories(Long categoryId, CategoryDTO categoryDTO) {
        Category categoryDB = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryDB.setCategoryName(categoryDTO.getCategoryName());
        categoryDB.setIconImage(categoryDTO.getIconImage());
        categoryDB.setSlug(categoryDTO.getSlug());
        Category saved = categoryRepository.save(categoryDB);
        return modelMapper.map(saved, CategoryDTO.class);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category categoryDB = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        categoryRepository.delete(categoryDB);
        return "Category deleted Successfully!!";
    }

    @Override
    public CategoryDTO updateIconImage(Long categoryId, MultipartFile file) throws IOException {

        Category categoryFromDb = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "CategoryId", categoryId));

        List<UploadedImage> uploadedImages = fileService.uploadImages(
                path,
                List.of(file));

        UploadedImage uploadedImage = uploadedImages.get(0);

        String oldPublicId = categoryFromDb.getIconImagePublicId();

        categoryFromDb.setIconImage(uploadedImage.url());
        categoryFromDb.setIconImagePublicId(uploadedImage.publicId());

        Category updatedCategory = categoryRepository.save(categoryFromDb);

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            fileService.deleteImage(oldPublicId);
        }

        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    public PageRequest buildPage(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageDetail = PageRequest.of(pageNumber, pageSize, sort);
        return pageDetail;
    }

    public CategoryResponse buildResponse(Page<Category> page) {
        List<CategoryDTO> categoryDTOs = page.stream().map(category -> modelMapper.map(category, CategoryDTO.class))
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

}

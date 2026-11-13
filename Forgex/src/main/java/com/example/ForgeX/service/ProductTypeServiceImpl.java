package com.example.ForgeX.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ForgeX.dto.ProductTypeDTO;
import com.example.ForgeX.dto.ProductTypeResponseDTO;
import com.example.ForgeX.exceptions.APIException;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Category;
import com.example.ForgeX.model.ProductType;
import com.example.ForgeX.repository.CategoryRepository;
import com.example.ForgeX.repository.ProductTypeRepository;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public ProductTypeDTO createProductType(ProductTypeDTO request, Long categoryId) {
        Category categoryFromDb = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        String name = require(request.getTypeName(), "ProductType name is required");
        if (productTypeRepository.existsByTypeNameIgnoreCase(name)) {
            throw new APIException("A productType named '" + name + "' already exists");
        }
        if (productTypeRepository.existsBySlugIgnoreCase(request.getSlug())) {
            throw new APIException("Another type already uses the slug " + request.getSlug() + ".");
        }
        ProductType productType = modelMapper.map(request, ProductType.class);
        productType.setCategory(categoryFromDb);
        ProductType savedType = productTypeRepository.save(productType);

        ProductTypeDTO productTypeDTO = modelMapper.map(savedType, ProductTypeDTO.class);
        // productTypeDTO.setCategoryId(categoryFromDb.getCategoryId());
        productTypeDTO.setCategoryName(categoryFromDb.getCategoryName());
        return productTypeDTO;
    }

    @Override
    public ProductTypeResponseDTO getAllType(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Page<ProductType> page = productTypeRepository.findAll(buildPage(pageNumber, pageSize, sortBy, sortOrder));
        return buildresponse(page);

    }

    @Override
    public String deleteProductType(Long productTypeId) {
        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", "productTypeId", productTypeId));
        productTypeRepository.delete(productType);
        return "ProductType is deleted Successfully!";
    }

    @Override
    public ProductTypeDTO updateProductType(Long productTypeId, ProductTypeDTO request) {
        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", "productTypeId", productTypeId));

        productType.setDisplayOrder(request.getDisplayOrder());
        productType.setSlug(request.getSlug());
        productType.setTypeName(request.getTypeName());
        productType.setActive(request.getActive());
        ProductTypeDTO productTypeDTO = modelMapper.map(productType, ProductTypeDTO.class);
        productTypeRepository.save(productType);
        return productTypeDTO;
    }

    private PageRequest buildPage(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    }

    private ProductTypeResponseDTO buildresponse(Page<ProductType> page) {
        List<ProductTypeDTO> pageDetails = page.getContent().stream()
                .map(p -> modelMapper.map(p, ProductTypeDTO.class))
                .toList();
        ProductTypeResponseDTO response = new ProductTypeResponseDTO();
        response.setContent(pageDetails);
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLastPage(page.isLast());
        return response;
    }

    private static String require(String raw, String message) {
        if (raw == null || raw.isBlank())
            throw new APIException(message);
        return raw.trim();
    }

}

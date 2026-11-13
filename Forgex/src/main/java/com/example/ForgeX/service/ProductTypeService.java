package com.example.ForgeX.service;

import com.example.ForgeX.dto.ProductTypeDTO;
import com.example.ForgeX.dto.ProductTypeResponseDTO;

public interface ProductTypeService {
     ProductTypeDTO createProductType(ProductTypeDTO request,Long categoryId);
     ProductTypeResponseDTO getAllType(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
     String deleteProductType(Long productTypeId);
      public ProductTypeDTO updateProductType(Long productTypeId, ProductTypeDTO request);
}

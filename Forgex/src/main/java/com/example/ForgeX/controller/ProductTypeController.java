package com.example.ForgeX.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ForgeX.config.AppConstants;
import com.example.ForgeX.dto.ProductTypeDTO;
import com.example.ForgeX.dto.ProductTypeResponseDTO;
import com.example.ForgeX.service.ProductTypeService;

@RestController
@RequestMapping("/api")
public class ProductTypeController {

    @Autowired
    private ProductTypeService productTypeService;

    @PostMapping("/public/categories/{categoryId}/product-Type")
    public ResponseEntity<ProductTypeDTO> createProductType(@RequestBody ProductTypeDTO request,
            @PathVariable Long categoryId) {
        ProductTypeDTO response = productTypeService.createProductType(request, categoryId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/public/product-Type")
    public ResponseEntity<ProductTypeResponseDTO> getAllProductType(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTSTYPE_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        ProductTypeResponseDTO response = productTypeService.getAllType(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/product-Type/{typeId}")
    public ResponseEntity<ProductTypeDTO> updateProductType(@PathVariable(name = "typeId") Long id,@RequestBody ProductTypeDTO request){
        ProductTypeDTO response=productTypeService.updateProductType(id, request);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }

    @DeleteMapping("/admin/product-Type/{typeId}")
    public ResponseEntity<String> deleteProductType(@PathVariable(name = "typeId") Long id){
        String response=productTypeService.deleteProductType(id);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }
}

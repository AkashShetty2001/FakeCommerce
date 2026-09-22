package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateProductRequestDto;
import com.fakecommerce.dtos.GetProductResponseDto;
import com.fakecommerce.dtos.GetProductWithDetailsDto;
import com.fakecommerce.services.ProductService;
import com.fakecommerce.utils.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController handles all HTTP requests related to products.
 * All endpoints return ResponseEntity with appropriate HTTP status codes and
 * respond with GetProductResponseDto / GetProductWithDetailsDto (mapped via
 * ProductMapper in the service layer) rather than the Product entity directly.
 * Base path: /api/v1/product
 */
@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final  ProductService productService;

    /**
     * Retrieve all products.
     * Endpoint: GET /api/v1/product/all
     * Response: 200 OK with the list of all products.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GetProductResponseDto>>> getAllProducts(){
        List<GetProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Retrieve a single product by id.
     * Endpoint: GET /api/v1/product/{id}
     * Response: 200 OK with the product details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetProductResponseDto>> getProductById(@PathVariable Long id){
        GetProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Retrieve a single product with its full details, including the resolved category.
     * Endpoint: GET /api/v1/product/{id}/details
     * Response: 200 OK with the product and its mapped CategoryResponseDto.
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<GetProductWithDetailsDto>> getProductWithDetailsById(@PathVariable Long id){
        GetProductWithDetailsDto product = productService.getProductWithDetailsById(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Product details retrieved successfully", product));
    }

    /**
     * Create a new product.
     * Endpoint: POST /api/v1/product
     * Request body: CreateProductRequestDto (categoryId must reference an existing category).
     * Response: 201 CREATED with the newly created product.
     */
    @PostMapping()
    public ResponseEntity<ApiResponse<GetProductResponseDto>> createProduct(@RequestBody CreateProductRequestDto requestDto){
        GetProductResponseDto product = productService.createProduct(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product created successfully", product));
    }

    /**
     * Delete a product by id.
     * Endpoint: DELETE /api/v1/product/{id}
     * Response: 200 OK after successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteProductById(@PathVariable Long id){
        productService.deleteProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    /**
     * Retrieve products filtered by category name.
     * Endpoint: GET /api/v1/product/search?categoryName=...
     * Response: 200 OK with the list of matching products.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GetProductResponseDto>>> getProductsByCategory(@RequestParam("categoryName") String category){
        List<GetProductResponseDto> products = productService.getProductsByCategory(category);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Retrieve all distinct category names present on products.
     * Endpoint: GET /api/v1/product/categories
     * Response: 200 OK with the list of distinct category names.
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getDistinctCategories(){
        List<String> categories = productService.getDistinctCategories();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Categories retrieved successfully", categories));
    }

    /**
     * Update an existing product. Only non-null fields on the request body are applied.
     * Endpoint: PUT /api/v1/product/{id}
     * Response: 200 OK with the updated product.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GetProductResponseDto>> updateProductById(@PathVariable Long id,@RequestBody CreateProductRequestDto requestDto){
        GetProductResponseDto product = productService.updateProductById(id, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Product updated successfully", product));
    }



}

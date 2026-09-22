package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CategoryResponseDto;
import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.services.CategoryService;
import com.fakecommerce.utils.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CategoryController handles all HTTP requests related to categories.
 * All endpoints return ResponseEntity with appropriate HTTP status codes and
 * respond with CategoryResponseDto (mapped via CategoryMapper in the service
 * layer) rather than the Category entity directly.
 * Base path: /api/v1/categories
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Retrieve all categories.
     * Endpoint: GET /api/v1/categories/all
     * Response: 200 OK with the list of all categories.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories(){
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Categories fetched successfully", categories));
    }

    /**
     * Retrieve a single category by id.
     * Endpoint: GET /api/v1/categories/{id}
     * Response: 200 OK with the category details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable("id") Long id){
        CategoryResponseDto category =  categoryService.getCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Category fetched successfully", category));
    }

    /**
     * Create a new category.
     * Endpoint: POST /api/v1/categories
     * Response: 201 CREATED with the newly created category and a Location header.
     */
    @PostMapping()
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(@RequestBody CreateCategoryRequestDto categoryRequestDto){
        CategoryResponseDto category =  categoryService.createCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/v1/categories/" + category.getId())
                .body(ApiResponse.success("Category created successfully", category));

    }

    /**
     * Delete a category by id.
     * Endpoint: DELETE /api/v1/categories/{id}
     * Response: 200 OK after successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCategoryById(@PathVariable Long id){
        categoryService.deleteCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Category deleted successfully", null));
    }

    /**
     * Update an existing category.
     * Endpoint: PUT /api/v1/categories/{id}
     * Response: 200 OK with the updated category details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategoryById(@PathVariable Long id,@RequestBody CreateCategoryRequestDto categoryRequestDto){
        CategoryResponseDto category = categoryService.updateCategoryById(id,categoryRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Category updated successfully", category));
    }



}

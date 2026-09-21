package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.schema.Category;
import com.fakecommerce.services.CategoryService;
import com.fakecommerce.utils.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Returns 200 OK with list of all categories
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories(){
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Categories fetched successfully", categories));
    }

    // Returns 200 OK with category details for specified ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable("id") Long id){
        Category category =  categoryService.getCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Category fetched successfully", category));
    }

    // Returns 201 CREATED with newly created category and Location header
    @PostMapping()
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody CreateCategoryRequestDto categoryRequestDto){
        Category category =  categoryService.createCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/v1/categories/" + category.getId())
                .body(ApiResponse.success("Category created successfully", category));

    }

    // Returns 200 OK after successful deletion
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCategoryById(@PathVariable Long id){
        categoryService.deleteCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Category deleted successfully", null));
    }

    // Returns 200 OK with updated category details
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategoryById(@PathVariable Long id,@RequestBody CreateCategoryRequestDto categoryRequestDto){
        Category category = categoryService.updateCategoryById(id,categoryRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Category updated successfully", category));
    }



}

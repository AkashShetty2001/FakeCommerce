package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.schema.Category;
import com.fakecommerce.services.CategoryService;
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
    public ResponseEntity<List<Category>> getAllCategories(){
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Returns 200 OK with category details for specified ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id){
        Category category =  categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // Returns 201 CREATED with newly created category and Location header
    @PostMapping()
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequestDto categoryRequestDto){
        Category category =  categoryService.createCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/v1/categories/" + category.getId())
                .body(category);
            
    }

    // Returns 204 NO CONTENT after successful deletion
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable Long id){
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    // Returns 200 OK with updated category details
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategoryById(@PathVariable Long id,@RequestBody CreateCategoryRequestDto categoryRequestDto){
        Category category = categoryService.updateCategoryById(id,categoryRequestDto);
        return ResponseEntity.ok(category);
    }



}

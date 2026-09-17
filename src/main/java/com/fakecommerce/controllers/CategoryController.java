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

    @GetMapping("/all")
    public List<Category> getAllCategories(){
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable("id") Long id){
        return categoryService.getCategoryById(id);
    }

    @PostMapping()
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequestDto categoryRequestDto){
        Category category =  categoryService.createCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/v1/categories/" + category.getId())
                .body(category);
            
    }

    @DeleteMapping("/{id}")
    public void deleteCategoryById(@PathVariable Long id){
        categoryService.deleteCategoryById(id);
    }

    @PutMapping("/{id}")
    public Category updateCategoryById(@PathVariable Long id,@RequestBody CreateCategoryRequestDto categoryRequestDto){
        return categoryService.updateCategoryById(id,categoryRequestDto);
    }



}

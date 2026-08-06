package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.schema.Category;
import com.fakecommerce.services.CategoryService;
import lombok.RequiredArgsConstructor;
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
    public Category getCategoryById(@PathVariable Long id){
        return categoryService.getCategoryById(id);
    }

    @PostMapping()
    public Category createCategory(@RequestBody CreateCategoryRequestDto categoryRequestDto){
        return categoryService.createCategory(categoryRequestDto);
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

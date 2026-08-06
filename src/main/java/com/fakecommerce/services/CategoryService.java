package com.fakecommerce.services;

import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.repository.CategoryRepository;
import com.fakecommerce.schema.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Category createCategory(CreateCategoryRequestDto categoryRequestDto){
        Category newCategory = Category.builder()
                .categoryName(categoryRequestDto.getCategoryName())
                .build();
        return categoryRepository.save(newCategory);
    }

    public void deleteCategoryById(Long id){
      if(!categoryRepository.existsById(id)){
          throw new RuntimeException("Category not found with id: " + id);
      }
        categoryRepository.deleteById(id);
    }

    public Category updateCategoryById(Long id, CreateCategoryRequestDto categoryRequestDto){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setCategoryName(categoryRequestDto.getCategoryName());
        return categoryRepository.save(category);
    }
}

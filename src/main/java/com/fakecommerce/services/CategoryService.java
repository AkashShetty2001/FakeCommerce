package com.fakecommerce.services;

import com.fakecommerce.dtos.CategoryResponseDto;
import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.exceptions.CategoryNotFoundException;
import com.fakecommerce.mappers.CategoryMapper;
import com.fakecommerce.repository.CategoryRepository;
import com.fakecommerce.schema.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDto> getAllCategories(){
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    public CategoryResponseDto getCategoryById(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        return categoryMapper.toResponseDto(category);
    }

    public CategoryResponseDto createCategory(CreateCategoryRequestDto categoryRequestDto){
        Category newCategory = categoryMapper.toEntity(categoryRequestDto);
        Category savedCategory = categoryRepository.save(newCategory);
        return categoryMapper.toResponseDto(savedCategory);
    }

    public void deleteCategoryById(Long id){
      if(!categoryRepository.existsById(id)){
          throw new CategoryNotFoundException("Category not found with id: " + id);
      }
        categoryRepository.deleteById(id);
    }

    public CategoryResponseDto updateCategoryById(Long id, CreateCategoryRequestDto categoryRequestDto){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        category.setCategoryName(categoryRequestDto.getCategoryName());
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updatedCategory);
    }
}

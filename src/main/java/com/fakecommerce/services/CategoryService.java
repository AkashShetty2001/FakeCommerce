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

/**
 * CategoryService contains all business logic related to categories.
 * Entity <-> DTO mapping is delegated to CategoryMapper (MapStruct).
 */
@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Get all categories from the database.
     *
     * @return a list of all categories mapped to CategoryResponseDto
     */
    public List<CategoryResponseDto> getAllCategories(){
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    /**
     * Get a single category by id.
     *
     * @param id the category ID to retrieve
     * @return CategoryResponseDto for the found category
     * @throws CategoryNotFoundException if no category exists with the given id
     */
    public CategoryResponseDto getCategoryById(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        return categoryMapper.toResponseDto(category);
    }

    /**
     * Create a new category.
     *
     * @param categoryRequestDto the request DTO with the category name
     * @return CategoryResponseDto for the newly created category
     */
    public CategoryResponseDto createCategory(CreateCategoryRequestDto categoryRequestDto){
        Category newCategory = categoryMapper.toEntity(categoryRequestDto);
        Category savedCategory = categoryRepository.save(newCategory);
        return categoryMapper.toResponseDto(savedCategory);
    }

    /**
     * Delete a category by id.
     *
     * @param id the category ID to delete
     * @throws CategoryNotFoundException if no category exists with the given id
     */
    public void deleteCategoryById(Long id){
      if(!categoryRepository.existsById(id)){
          throw new CategoryNotFoundException("Category not found with id: " + id);
      }
        categoryRepository.deleteById(id);
    }

    /**
     * Update an existing category's name.
     *
     * @param id the category ID to update
     * @param categoryRequestDto the request DTO with the new category name
     * @return CategoryResponseDto for the updated category
     * @throws CategoryNotFoundException if no category exists with the given id
     */
    public CategoryResponseDto updateCategoryById(Long id, CreateCategoryRequestDto categoryRequestDto){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        category.setCategoryName(categoryRequestDto.getCategoryName());
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updatedCategory);
    }
}

package com.fakecommerce.mappers;

import com.fakecommerce.dtos.CategoryResponseDto;
import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.schema.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CreateCategoryRequestDto dto);

    CategoryResponseDto toResponseDto(Category category);
}

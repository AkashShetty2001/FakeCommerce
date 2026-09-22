package com.fakecommerce.mappers;

import com.fakecommerce.dtos.CreateProductRequestDto;
import com.fakecommerce.dtos.GetProductResponseDto;
import com.fakecommerce.dtos.GetProductWithDetailsDto;
import com.fakecommerce.schema.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Maps between the Product entity and its request/response DTOs.
 * Category is resolved separately in ProductService (it needs a repository
 * lookup from categoryId), so it is excluded from the entity-facing mappings
 * here and wired up manually after mapping. The Category -> CategoryResponseDto
 * conversion for the "with details" response is delegated to CategoryMapper.
 */
@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ProductMapper {

    // Builds a new Product from the create request; category is set by the service.
    @Mapping(target = "category", ignore = true)
    Product toEntity(CreateProductRequestDto dto);

    // Applies non-null fields from the update request onto an existing Product;
    // category is re-assigned by the service only when categoryId is provided.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(CreateProductRequestDto dto, @MappingTarget Product product);

    GetProductResponseDto toResponseDto(Product product);

    List<GetProductResponseDto> toResponseDtoList(List<Product> products);

    // Includes the mapped category via CategoryMapper#toResponseDto.
    GetProductWithDetailsDto toDetailsResponseDto(Product product);
}

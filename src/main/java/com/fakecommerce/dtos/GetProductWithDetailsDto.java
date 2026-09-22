package com.fakecommerce.dtos;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetProductWithDetailsDto extends GetProductResponseDto {

    // Mapped from Product.category via CategoryMapper so entities are never exposed directly.
    private CategoryResponseDto category;
}

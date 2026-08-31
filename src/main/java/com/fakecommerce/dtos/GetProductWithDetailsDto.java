package com.fakecommerce.dtos;

import com.fakecommerce.schema.Category;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetProductWithDetailsDto extends GetProductResponseDto {

    private Category category;
}

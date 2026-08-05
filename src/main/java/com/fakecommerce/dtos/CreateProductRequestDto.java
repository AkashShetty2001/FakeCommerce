package com.fakecommerce.dtos;

import com.fakecommerce.schema.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequestDto {

    private String title;

    private BigDecimal price;

    private String image;

    private Category category;

    private String ratings;

    private String description;
}

package com.fakecommerce.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GetProductResponseDto {


    private Long id;

    private String title;

    private BigDecimal price;

    private String image;

    private BigDecimal ratings;

    private String description;
}

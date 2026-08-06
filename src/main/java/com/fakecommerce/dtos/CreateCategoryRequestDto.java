package com.fakecommerce.dtos;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequestDto {
    private String categoryName;
}

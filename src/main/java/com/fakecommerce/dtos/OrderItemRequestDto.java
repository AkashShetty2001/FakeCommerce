package com.fakecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderItemRequestDto represents a single line item in a CreateOrderRequestDto.
 * When creating an order, the client specifies which product and how many units are desired.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDto {

    /**
     * The ID of the product being ordered.
     */
    private Long productId;

    /**
     * The quantity of the product to order (must be > 0).
     */
    private Integer quantity;
}

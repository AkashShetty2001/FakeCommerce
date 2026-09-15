package com.fakecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItemResponseDto represents a single line item in an order response.
 * It includes enriched product information (title, price) and calculated subtotal.
 * Subtotal is computed as: price × quantity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDto {

    /**
     * The ID of the product in this line item.
     */
    private Long productId;

    /**
     * The title/name of the product (denormalized from the Product entity for convenience).
     */
    private String productTitle;

    /**
     * The unit price of the product at the time the order was placed.
     */
    private BigDecimal price;

    /**
     * The quantity ordered of this product.
     */
    private Integer quantity;

    /**
     * The subtotal for this line item: price × quantity.
     * Calculated in the service layer during response mapping.
     */
    private BigDecimal subtotal;
}

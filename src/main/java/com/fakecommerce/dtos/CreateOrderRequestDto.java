package com.fakecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CreateOrderRequestDto represents the request body for creating a new order.
 * The client provides a list of OrderItemRequestDto, each specifying a product ID and quantity.
 * The OrderService will validate the items, calculate the total amount, and persist the Order and OrderItems.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequestDto {

    /**
     * List of line items to include in the order.
     * Must not be null or empty. Each item specifies a product ID and desired quantity.
     */
    private List<OrderItemRequestDto> items;
}

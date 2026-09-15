package com.fakecommerce.dtos;

import com.fakecommerce.schema.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * OrderResponseDto represents the complete order response returned by the Order API endpoints.
 * It includes the order ID, status, all line items (enriched), and the calculated total amount.
 * This DTO is used in GET, POST, PUT endpoints to provide a consistent order representation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    /**
     * The unique identifier of the order.
     */
    private Long id;

    /**
     * The current status of the order (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED).
     */
    private OrderStatus status;

    /**
     * The list of line items in the order.
     * Each item includes product details, quantity, and subtotal.
     */
    private List<OrderItemResponseDto> items;

    /**
     * The total amount for the entire order (sum of all line item subtotals).
     * Calculated and stored in the database via the Order entity.
     */
    private BigDecimal totalAmount;
}

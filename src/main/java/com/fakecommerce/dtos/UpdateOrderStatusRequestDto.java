package com.fakecommerce.dtos;

import com.fakecommerce.schema.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateOrderStatusRequestDto represents the request body for updating an order's status.
 * The client specifies the new status, and the service updates and persists the change.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusRequestDto {

    /**
     * The new status to set on the order.
     * Valid values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED.
     */
    private OrderStatus status;
}

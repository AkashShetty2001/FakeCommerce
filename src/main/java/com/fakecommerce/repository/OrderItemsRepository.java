package com.fakecommerce.repository;

import com.fakecommerce.schema.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderItemsRepository provides database access for OrderItems entities.
 * OrderItems represents the junction entity linking Orders and Products (many-to-many).
 */
@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

    /**
     * Find all OrderItems belonging to a specific order.
     * Used in the service layer to fetch line items when building OrderResponseDto.
     *
     * @param orderId the ID of the order to fetch items for
     * @return a list of OrderItems associated with the given order
     */
    List<OrderItems> findByOrderId(Long orderId);
}

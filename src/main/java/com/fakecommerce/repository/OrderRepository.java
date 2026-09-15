package com.fakecommerce.repository;

import com.fakecommerce.schema.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * OrderRepository provides database access for Order entities.
 * Extends JpaRepository to inherit standard CRUD operations (save, find, delete, etc.).
 * Additional custom query methods can be added here as needed.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}

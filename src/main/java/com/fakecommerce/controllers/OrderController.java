package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateOrderRequestDto;
import com.fakecommerce.dtos.OrderResponseDto;
import com.fakecommerce.dtos.UpdateOrderStatusRequestDto;
import com.fakecommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController handles all HTTP requests related to orders.
 * All endpoints follow REST conventions and return ResponseEntity with appropriate HTTP status codes.
 * HTTP Status: 201 CREATED (POST), 200 OK (GET, PUT), 204 NO CONTENT (DELETE).
 * Constructor injection via @RequiredArgsConstructor injects the OrderService.
 * Base path: /api/v1/orders
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order.
     * Endpoint: POST /api/v1/orders
     * Request body: CreateOrderRequestDto containing a list of items to order.
     * Response: 201 CREATED with OrderResponseDto containing order ID, status (PENDING), items, and total amount.
     *
     * @param requestDto the request DTO with the items to order
     * @return ResponseEntity with 201 CREATED and OrderResponseDto
     */
    @PostMapping()
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody CreateOrderRequestDto requestDto) {
        OrderResponseDto order = orderService.createOrder(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Retrieve all orders.
     * Endpoint: GET /api/v1/orders/all
     * Response: 200 OK with list of OrderResponseDto for all orders in the system.
     *
     * @return ResponseEntity with 200 OK and List<OrderResponseDto> (empty list if none exist)
     */
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        List<OrderResponseDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieve a single order by ID.
     * Endpoint: GET /api/v1/orders/{id}
     * Response: 200 OK with OrderResponseDto containing the order details and all items.
     *
     * @param id the order ID to retrieve
     * @return ResponseEntity with 200 OK and OrderResponseDto for the requested order
     * @throws RuntimeException if order is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable("id") Long id) {
        OrderResponseDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Update the status of an order.
     * Endpoint: PUT /api/v1/orders/{id}/status
     * Request body: UpdateOrderStatusRequestDto specifying the new status.
     * Response: 200 OK with OrderResponseDto with the updated status.
     * Allowed status values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED.
     *
     * @param id the order ID to update
     * @param requestDto the request DTO with the new status
     * @return ResponseEntity with 200 OK and OrderResponseDto representing the updated order
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequestDto requestDto) {
        OrderResponseDto order = orderService.updateOrderStatus(id, requestDto);
        return ResponseEntity.ok(order);
    }

    /**
     * Delete an order by ID (soft-delete).
     * Endpoint: DELETE /api/v1/orders/{id}
     * The order is soft-deleted (marked as deleted) via the @SQLDelete annotation.
     * Response: 204 NO CONTENT with no body.
     *
     * @param id the order ID to delete
     * @return ResponseEntity with 204 NO CONTENT
     * @throws IllegalArgumentException if order does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}


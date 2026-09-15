package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateOrderRequestDto;
import com.fakecommerce.dtos.OrderResponseDto;
import com.fakecommerce.dtos.UpdateOrderStatusRequestDto;
import com.fakecommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController handles all HTTP requests related to orders.
 * All endpoints follow REST conventions and return plain DTOs (no ResponseEntity wrapper).
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
     * Response: OrderResponseDto with order ID, status (PENDING), items, and total amount.
     *
     * @param requestDto the request DTO with the items to order
     * @return OrderResponseDto representing the newly created order
     */
    @PostMapping()
    public OrderResponseDto createOrder(@RequestBody CreateOrderRequestDto requestDto) {
        return orderService.createOrder(requestDto);
    }

    /**
     * Retrieve all orders.
     * Endpoint: GET /api/v1/orders/all
     * Response: List of OrderResponseDto for all orders in the system.
     *
     * @return List<OrderResponseDto> of all orders (empty list if none exist)
     */
    @GetMapping("/all")
    public List<OrderResponseDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    /**
     * Retrieve a single order by ID.
     * Endpoint: GET /api/v1/orders/{id}
     * Response: OrderResponseDto containing the order details and all items.
     *
     * @param id the order ID to retrieve
     * @return OrderResponseDto for the requested order
     * @throws RuntimeException if order is not found
     */
    @GetMapping("/{id}")
    public OrderResponseDto getOrderById(@PathVariable("id") Long id) {
        return orderService.getOrderById(id);
    }

    /**
     * Update the status of an order.
     * Endpoint: PUT /api/v1/orders/{id}/status
     * Request body: UpdateOrderStatusRequestDto specifying the new status.
     * Response: OrderResponseDto with the updated status.
     * Allowed status values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED.
     *
     * @param id the order ID to update
     * @param requestDto the request DTO with the new status
     * @return OrderResponseDto representing the updated order
     */
    @PutMapping("/{id}/status")
    public OrderResponseDto updateOrderStatus(@PathVariable("id") Long id, @RequestBody UpdateOrderStatusRequestDto requestDto) {
        return orderService.updateOrderStatus(id, requestDto);
    }

    /**
     * Delete an order by ID (soft-delete).
     * Endpoint: DELETE /api/v1/orders/{id}
     * The order is soft-deleted (marked as deleted) via the @SQLDelete annotation.
     * Response: void (HTTP 200 OK with no body).
     *
     * @param id the order ID to delete
     * @throws IllegalArgumentException if order does not exist
     */
    @DeleteMapping("/{id}")
    public void deleteOrderById(@PathVariable("id") Long id) {
        orderService.deleteOrderById(id);
    }
}


package com.fakecommerce.services;

import com.fakecommerce.dtos.*;
import com.fakecommerce.exceptions.ResourceNotFoundException;
import com.fakecommerce.repository.OrderItemsRepository;
import com.fakecommerce.repository.OrderRepository;
import com.fakecommerce.repository.ProductRepository;
import com.fakecommerce.schema.Order;
import com.fakecommerce.schema.OrderItems;
import com.fakecommerce.schema.OrderStatus;
import com.fakecommerce.schema.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderService contains all business logic related to orders.
 * It handles order creation, retrieval, status updates, and deletion.
 * Uses constructor injection for repositories (@RequiredArgsConstructor from Lombok).
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new order from the provided CreateOrderRequestDto.
     * Process:
     *   1. Validate that items list is not null or empty.
     *   2. For each item, fetch the Product and validate quantity > 0.
     *   3. Calculate total order amount (sum of price × quantity for all items).
     *   4. Persist an Order with PENDING status and the calculated total.
     *   5. Persist one OrderItems row per line item.
     *   6. Return the mapped OrderResponseDto.
     *
     * @param requestDto the request containing a list of items to order
     * @return OrderResponseDto representing the newly created order
     * @throws IllegalArgumentException if items list is empty or quantity is invalid
     * @throws RuntimeException if a product is not found
     */
    public OrderResponseDto createOrder(CreateOrderRequestDto requestDto) {
        // Validate items list is not null or empty
        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items list cannot be null or empty.");
        }

        // Validate each item and fetch corresponding products
        List<OrderItems> orderItemsList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDto itemDto : requestDto.getItems()) {
            // Fetch the product; throw exception if not found
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            // Validate quantity
            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for product id: " + itemDto.getProductId());
            }

            // Calculate subtotal for this item (price × quantity)
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // Create an OrderItems entity (we'll link it to the order after saving the order)
            OrderItems orderItem = OrderItems.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .build();
            orderItemsList.add(orderItem);
        }

        // Create and save the Order with PENDING status and calculated total
        Order newOrder = Order.builder()
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .build();
        Order savedOrder = orderRepository.save(newOrder);

        // Link each OrderItems to the saved Order and save them
        for (OrderItems item : orderItemsList) {
            item.setOrder(savedOrder);
        }
        orderItemsRepository.saveAll(orderItemsList);

        // Map and return the OrderResponseDto
        return mapToOrderResponseDto(savedOrder);
    }

    /**
     * Retrieve all orders from the database.
     * Maps each Order entity to an OrderResponseDto.
     *
     * @return a list of all OrderResponseDto (empty list if no orders exist)
     */
    public List<OrderResponseDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToOrderResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single order by ID.
     * Fetches the Order and maps it to OrderResponseDto, including all related items.
     *
     * @param id the order ID to retrieve
     * @return OrderResponseDto for the found order
     * @throws RuntimeException if order is not found
     */
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderResponseDto(order);
    }

    /**
     * Update the status of an existing order.
     * Fetches the Order, updates its status, persists the change, and returns the updated OrderResponseDto.
     *
     * @param id the order ID to update
     * @param requestDto the request containing the new status
     * @return OrderResponseDto representing the updated order
     * @throws RuntimeException if order is not found
     */
    public OrderResponseDto updateOrderStatus(Long id, UpdateOrderStatusRequestDto requestDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(requestDto.getStatus());
        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponseDto(updatedOrder);
    }

    /**
     * Delete an order by ID (soft-delete via @SQLDelete annotation on Order entity).
     * Checks if the order exists before attempting deletion.
     *
     * @param id the order ID to delete
     * @throws IllegalArgumentException if order does not exist
     */
    public void deleteOrderById(Long id) {
        // Check if the order exists in the database
        if (!orderRepository.existsById(id)) {
            // Throw an exception if it's missing
            throw new IllegalArgumentException("Order with ID " + id + " does not exist.");
        }

        // Delete the order (soft-delete via @SQLDelete annotation)
        orderRepository.deleteById(id);
    }

    /**
     * Private helper method to map an Order entity to an OrderResponseDto.
     * Fetches all OrderItems for the order, enriches them with product details,
     * and assembles the complete response DTO.
     * Used by all methods that return OrderResponseDto.
     *
     * @param order the Order entity to map
     * @return OrderResponseDto with populated items, subtotals, and total amount
     */
    private OrderResponseDto mapToOrderResponseDto(Order order) {
        // Fetch all OrderItems for this order
        List<OrderItems> orderItems = orderItemsRepository.findByOrderId(order.getId());

        // Convert each OrderItems to OrderItemResponseDto
        List<OrderItemResponseDto> itemResponseDtos = orderItems.stream()
                .map(item -> OrderItemResponseDto.builder()
                        .productId(item.getProduct().getId())
                        .productTitle(item.getProduct().getTitle())
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        // Build and return the OrderResponseDto
        return OrderResponseDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .items(itemResponseDtos)
                .totalAmount(order.getTotalAmount())
                .build();
    }
}

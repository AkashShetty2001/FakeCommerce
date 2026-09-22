package com.fakecommerce.mappers;

import com.fakecommerce.dtos.OrderItemResponseDto;
import com.fakecommerce.dtos.OrderResponseDto;
import com.fakecommerce.schema.Order;
import com.fakecommerce.schema.OrderItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps between Order/OrderItems entities and their response DTOs.
 * Line items are fetched separately in OrderService (to avoid relying on the
 * lazy-loaded Order.items collection outside a transaction), so toResponseDto
 * takes the order and its items as two source parameters.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Enriches a line item with product details and a computed subtotal (price x quantity).
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productTitle", source = "product.title")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    OrderItemResponseDto toItemResponseDto(OrderItems item);

    List<OrderItemResponseDto> toItemResponseDtoList(List<OrderItems> items);

    @Mapping(target = "items", source = "items")
    OrderResponseDto toResponseDto(Order order, List<OrderItems> items);

    default BigDecimal calculateSubtotal(OrderItems item) {
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}

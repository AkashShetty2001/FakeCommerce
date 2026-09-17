# Order API - Code Examples & Architecture

## 1. Architecture Overview

```
HTTP Request
    ↓
OrderController (REST layer)
    ↓
OrderService (Business logic)
    ├→ OrderRepository (CRUD for Order)
    ├→ OrderItemsRepository (CRUD for OrderItems)
    └→ ProductRepository (Fetch Product details)
    ↓
Database (via JPA/Hibernate)
    ├→ orders table
    ├→ order_products table (OrderItems entity)
    └→ products table
```

---

## 2. Data Flow: Creating an Order

### Step 1: Client sends HTTP POST request
```json
POST /api/v1/orders
Content-Type: application/json

{
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 2, "quantity": 3}
  ]
}
```
**Maps to:** `CreateOrderRequestDto`

---

### Step 2: Controller receives and delegates
```java
// OrderController.java
@PostMapping()
public OrderResponseDto createOrder(@RequestBody CreateOrderRequestDto requestDto) {
    return orderService.createOrder(requestDto);  // ← Delegates to service
}
```

---

### Step 3: Service processes the order
```java
// OrderService.java - Simplified version
public OrderResponseDto createOrder(CreateOrderRequestDto requestDto) {
    
    // 1. Validate items list
    if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
        throw new IllegalArgumentException("Order items cannot be empty");
    }
    
    // 2. Process each item
    List<OrderItems> orderItemsList = new ArrayList<>();
    BigDecimal totalAmount = BigDecimal.ZERO;
    
    for (OrderItemRequestDto itemDto : requestDto.getItems()) {
        
        // Fetch product from database
        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Validate quantity
        if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        
        // Calculate subtotal
        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(itemDto.getQuantity()));
        totalAmount = totalAmount.add(subtotal);
        
        // Create OrderItems entity
        OrderItems orderItem = OrderItems.builder()
                .product(product)
                .quantity(itemDto.getQuantity())
                .build();
        orderItemsList.add(orderItem);
    }
    
    // 3. Create Order entity
    Order newOrder = Order.builder()
            .status(OrderStatus.PENDING)
            .totalAmount(totalAmount)  // ← Persist total
            .build();
    Order savedOrder = orderRepository.save(newOrder);
    
    // 4. Link and save OrderItems
    for (OrderItems item : orderItemsList) {
        item.setOrder(savedOrder);
    }
    orderItemsRepository.saveAll(orderItemsList);
    
    // 5. Map to response DTO and return
    return mapToOrderResponseDto(savedOrder);
}
```

---

### Step 4: Database state after creation
```
orders table:
├── id: 1
├── status: 0 (ORDINAL for PENDING)
├── total_amount: 50.00
├── created_at: 2024-09-09 20:45:00
├── updated_at: 2024-09-09 20:45:00
└── deleted_at: null

order_products table:
├── id: 1, order_id: 1, product_id: 1, quantity: 2, created_at: ..., deleted_at: null
└── id: 2, order_id: 1, product_id: 2, quantity: 3, created_at: ..., deleted_at: null
```

---

### Step 5: Response mapping to client
```java
// Service's private helper method
private OrderResponseDto mapToOrderResponseDto(Order order) {
    
    // Fetch all line items for this order
    List<OrderItems> orderItems = orderItemsRepository.findByOrderId(order.getId());
    
    // Convert to response DTOs
    List<OrderItemResponseDto> itemResponseDtos = orderItems.stream()
            .map(item -> OrderItemResponseDto.builder()
                    .productId(item.getProduct().getId())
                    .productTitle(item.getProduct().getTitle())
                    .price(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build())
            .collect(Collectors.toList());
    
    // Build response
    return OrderResponseDto.builder()
            .id(order.getId())
            .status(order.getStatus())
            .items(itemResponseDtos)
            .totalAmount(order.getTotalAmount())
            .build();
}
```

---

### Step 6: Client receives response
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "status": "PENDING",
  "items": [
    {
      "productId": 1,
      "productTitle": "Laptop",
      "price": 999.99,
      "quantity": 2,
      "subtotal": 1999.98
    },
    {
      "productId": 2,
      "productTitle": "Mouse",
      "price": 25.00,
      "quantity": 3,
      "subtotal": 75.00
    }
  ],
  "totalAmount": 2074.98
}
```

---

## 3. Entity Relationships

### Order Entity
```
Order
├── id (PK)
├── status (enum: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
├── total_amount (calculated sum of line items)
├── created_at (audited)
├── updated_at (audited)
├── deleted_at (soft-delete marker)
└── [One-to-Many] orderItems ← OrderItems.order (LAZY fetch)
```

### OrderItems Entity
```
OrderItems
├── id (PK)
├── order_id (FK) → Order (required)
├── product_id (FK) → Product (required)
├── quantity (number of units)
├── created_at (audited)
├── updated_at (audited)
└── deleted_at (soft-delete marker)
```

### Relationship Diagram
```
One Order ─────────────────── Many OrderItems
         (one-to-many)               ↓
                              Each OrderItems
                              links to One Product
```

---

## 4. DTO Layers

### Request DTOs
```
CreateOrderRequestDto
└── items: List<OrderItemRequestDto>
    └── OrderItemRequestDto
        ├── productId: Long
        └── quantity: Integer
```

### Response DTOs
```
OrderResponseDto
├── id: Long
├── status: OrderStatus
├── items: List<OrderItemResponseDto>
│   └── OrderItemResponseDto
│       ├── productId: Long
│       ├── productTitle: String
│       ├── price: BigDecimal
│       ├── quantity: Integer
│       └── subtotal: BigDecimal (calculated)
└── totalAmount: BigDecimal
```

---

## 5. Validation Flow

```
CreateOrderRequestDto
    ↓
✗ items == null or empty?
    └→ throw IllegalArgumentException
    ↓
✓ For each OrderItemRequestDto:
    ├─ ✗ product doesn't exist?
    │   └→ throw RuntimeException
    ├─ ✗ quantity <= 0?
    │   └→ throw IllegalArgumentException
    ├─ ✓ Product found
    └─ ✓ Quantity valid
    ↓
✓ Order created successfully
```

---

## 6. Exception Handling Strategy

### RuntimeException — Resource Not Found
```java
// Used when:
// - Product doesn't exist
// - Order doesn't exist
// - External resource is missing

Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
```
**Result:** HTTP 500 Internal Server Error (Spring converts to JSON error response)

### IllegalArgumentException — Validation Failure
```java
// Used when:
// - Items list is empty
// - Quantity is invalid
// - Order doesn't exist (for delete)

if (itemDto.getQuantity() <= 0) {
    throw new IllegalArgumentException("Quantity must be > 0");
}
```
**Result:** HTTP 500 Internal Server Error (Spring converts to JSON error response)

---

## 7. Annotation Guide

### Repository
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // @Repository makes Spring recognize this as a DAO
    // JpaRepository provides CRUD + pagination/sorting
}
```

### Service
```java
@Service
@RequiredArgsConstructor  // ← Lombok generates constructor for final fields
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;
    // Dependencies injected via constructor
}
```

### Controller
```java
@RestController  // ← Returns JSON, not HTML
@RequestMapping("/api/v1/orders")  // ← Base URL path
@RequiredArgsConstructor  // ← Injects OrderService
public class OrderController {
    private final OrderService orderService;
    
    @PostMapping()  // ← POST to /api/v1/orders
    public OrderResponseDto createOrder(@RequestBody CreateOrderRequestDto requestDto) {
        return orderService.createOrder(requestDto);
    }
}
```

### Entity
```java
@Entity
@Table(name = "orders")
@Data  // ← Generates getters, setters, equals, hashCode, toString
@Builder  // ← Builder pattern for creating instances
@AllArgsConstructor  // ← Constructor with all fields
@NoArgsConstructor  // ← No-arg constructor (required by JPA)
@SQLDelete(sql = "UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")  // ← Soft-delete pattern
public class Order extends BaseEntity {
    private OrderStatus status;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
}
```

### DTO
```java
@Data  // ← Lombok: getters, setters, equals, hashCode, toString
@Builder  // ← Builder pattern
@AllArgsConstructor  // ← All fields constructor
@NoArgsConstructor  // ← No-arg constructor
public class OrderResponseDto {
    private Long id;
    private OrderStatus status;
    private List<OrderItemResponseDto> items;
    private BigDecimal totalAmount;
}
```

---

## 8. Stream API Usage (Functional Mapping)

### Converting List of Entities to List of DTOs
```java
// Traditional (commented out in code):
List<OrderItemResponseDto> itemResponseDtos = new ArrayList<>();
for (OrderItems item : orderItems) {
    itemResponseDtos.add(OrderItemResponseDto.builder()
            .productId(item.getProduct().getId())
            // ... more fields
            .build());
}

// Modern (Stream API):
List<OrderItemResponseDto> itemResponseDtos = orderItems.stream()
        .map(item -> OrderItemResponseDto.builder()
                .productId(item.getProduct().getId())
                // ... more fields
                .build())
        .collect(Collectors.toList());
```
**Benefit:** More concise, functional, and idiomatic Java 8+

---

## 9. Constructor Injection vs Field Injection

### Field Injection ❌ (Not used in this project)
```java
@Service
public class OrderService {
    @Autowired  // ← Field injection
    private OrderRepository orderRepository;
}
```
**Disadvantages:** Hard to test, hides dependencies, can't mark as final

### Constructor Injection ✅ (Used throughout)
```java
@Service
@RequiredArgsConstructor  // ← Lombok generates the constructor
public class OrderService {
    private final OrderRepository orderRepository;  // ← Explicit dependency
    // Constructor automatically generated:
    // public OrderService(OrderRepository orderRepository) {
    //     this.orderRepository = orderRepository;
    // }
}
```
**Advantages:** Testable, explicit, dependencies marked as final, immutable

---

## 10. Soft Delete Pattern

### How it works:
```sql
-- When entity has @SQLDelete and @SQLRestriction
@SQLDelete(sql = "UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Order extends BaseEntity {
    // ...
}
```

### Delete operation:
```java
orderRepository.deleteById(1);
```
**Instead of:** `DELETE FROM orders WHERE id = 1`
**Executes:** `UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = 1`

### Fetch operation:
```java
orderRepository.findAll();
```
**Automatically adds:** `WHERE deleted_at IS NULL` to all queries

**Benefit:** Data never lost, maintains audit trail, allows recovery

---

## 11. Big Decimal for Financial Data

### Why BigDecimal?
```java
// ❌ DON'T use double/float for money
double total = 19.99 + 0.01;  // Result: 19.999999999999998 (precision error!)

// ✅ DO use BigDecimal for money
BigDecimal total = new BigDecimal("19.99").add(new BigDecimal("0.01"));
// Result: 20.00 (exact)
```

### Usage in Order API:
```java
Order.totalAmount: BigDecimal  // Total order cost
Product.price: BigDecimal      // Product unit price
OrderItemResponseDto.subtotal: BigDecimal  // price × quantity

// Calculation:
BigDecimal subtotal = product.getPrice()
        .multiply(BigDecimal.valueOf(item.getQuantity()));
```

---

## 12. Order Status State Machine

```
┌─────────────────────────────────────────────────────────┐
│                   Order Status Flow                      │
└─────────────────────────────────────────────────────────┘

Initial: PENDING
    ↓
    (Order placed, awaiting payment/fulfillment)
    ↓
PROCESSING
    ↓
    (Paid, being picked/packed)
    ↓
SHIPPED
    ↓
    (In transit to customer)
    ↓
DELIVERED
    ↓
    (Customer received)
    └─────────→ CANCELLED (at any point)

Valid Statuses:
- PENDING
- PROCESSING
- SHIPPED
- DELIVERED
- CANCELLED
```

**Note:** The API allows any status update. Production code should validate state transitions (e.g., can't go from DELIVERED back to PENDING).

---

## 13. Testing Examples

### Unit Test: createOrder with valid items
```java
@Test
public void testCreateOrderSuccess() {
    // Given
    Product product1 = Product.builder().id(1L).price(new BigDecimal("10.00")).build();
    Product product2 = Product.builder().id(2L).price(new BigDecimal("20.00")).build();
    
    OrderItemRequestDto item1 = OrderItemRequestDto.builder()
            .productId(1L).quantity(2).build();
    OrderItemRequestDto item2 = OrderItemRequestDto.builder()
            .productId(2L).quantity(1).build();
    
    CreateOrderRequestDto request = CreateOrderRequestDto.builder()
            .items(Arrays.asList(item1, item2)).build();
    
    // Mock repositories
    when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
    
    // When
    OrderResponseDto response = orderService.createOrder(request);
    
    // Then
    assertEquals(BigDecimal.valueOf(40.00), response.getTotalAmount());
    assertEquals(OrderStatus.PENDING, response.getStatus());
    assertEquals(2, response.getItems().size());
}
```

### Integration Test: POST /api/v1/orders
```java
@Test
public void testCreateOrderEndpoint() throws Exception {
    CreateOrderRequestDto request = CreateOrderRequestDto.builder()
            .items(Arrays.asList(
                OrderItemRequestDto.builder().productId(1L).quantity(2).build()
            ))
            .build();
    
    mockMvc.perform(post("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalAmount").value(20.00));
}
```

---

This comprehensive guide covers the complete Order API architecture, data flow, patterns, and examples!

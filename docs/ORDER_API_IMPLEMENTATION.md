# Order API Implementation Summary

## Overview
This document summarizes the complete Order API implementation for the fakecommerce project. The implementation follows the existing code conventions established by the Product and Category APIs.

---

## Files Created

### 1. Database Migration
**File:** `src/main/resources/db/migration/V2__add_order_total_and_numeric_ratings.sql`

- **Purpose:** Adds the `total_amount` column to the `orders` table and converts `ratings` from VARCHAR to DECIMAL.
- **Changes:**
  ```sql
  ALTER TABLE orders ADD COLUMN total_amount DECIMAL(38, 2);
  ALTER TABLE products MODIFY COLUMN ratings DECIMAL(2, 1);
  ```
- **Important Note:** If your local development database has any non-numeric text in the `ratings` column, the `MODIFY COLUMN` statement will fail. In that case, you have two options:
  1. Manually clean the existing ratings data before running Flyway.
  2. Wipe the local dev database and let Flyway recreate it from scratch.

---

## Files Modified

### 1. Entity: Order
**File:** `src/main/java/com/fakecommerce/schema/Order.java`

**Changes:**
- Added `BigDecimal totalAmount` field with `@Column(name = "total_amount")` annotation.
- Added Javadoc comment explaining the field's purpose.
- Imported `java.math.BigDecimal` and `jakarta.persistence.Column`.

**Purpose:** Stores the calculated total amount (sum of all line items) for each order.

---

### 2. Entity: Product
**File:** `src/main/java/com/fakecommerce/schema/Product.java`

**Changes:**
- Changed `ratings` field type from `String` to `BigDecimal`.
- Added Javadoc comment explaining the numeric rating format.

**Purpose:** Allows ratings to be numeric (e.g., 4.5 out of 5) rather than free-text strings.

---

### 3. DTO: CreateProductRequestDto
**File:** `src/main/java/com/fakecommerce/dtos/CreateProductRequestDto.java`

**Changes:**
- Changed `ratings` field type from `String` to `BigDecimal` (type follow-through from the Product entity change).

---

### 4. DTO: GetProductResponseDto
**File:** `src/main/java/com/fakecommerce/dtos/GetProductResponseDto.java`

**Changes:**
- Changed `ratings` field type from `String` to `BigDecimal` (type follow-through from the Product entity change).

---

### 5. Repository: OrderItemsRepository
**File:** `src/main/java/com/fakecommerce/repository/OrderItemsRepository.java`

**Changes:**
- Added method: `List<OrderItems> findByOrderId(Long orderId);`
- Added Javadoc comments explaining the repository's purpose and the new method.

**Purpose:** Enables the service to fetch all line items (OrderItems) belonging to a specific order.

---

### 6. Service: OrderService
**File:** `src/main/java/com/fakecommerce/services/OrderService.java`

**Completely rewritten** from an empty stub to a fully functional service.

**Methods:**
- **`createOrder(CreateOrderRequestDto requestDto)`**
  - Validates the items list (non-null, non-empty).
  - Validates each product exists and quantity > 0.
  - Calculates order total amount (sum of price × quantity).
  - Persists an Order with PENDING status and calculated total.
  - Persists all OrderItems line items.
  - Returns OrderResponseDto.

- **`getAllOrders()`**
  - Fetches all orders from the database.
  - Maps each to OrderResponseDto.
  - Returns a list of all orders.

- **`getOrderById(Long id)`**
  - Fetches a single order by ID.
  - Throws RuntimeException if not found.
  - Returns OrderResponseDto with full details including items.

- **`updateOrderStatus(Long id, UpdateOrderStatusRequestDto requestDto)`**
  - Updates the status of an existing order.
  - Throws RuntimeException if order not found.
  - Returns updated OrderResponseDto.

- **`deleteOrderById(Long id)`**
  - Performs soft-delete via @SQLDelete annotation.
  - Throws IllegalArgumentException if order does not exist.

- **`mapToOrderResponseDto(Order order)` (private helper)**
  - Converts an Order entity to OrderResponseDto.
  - Fetches all OrderItems for the order.
  - Enriches each item with product details and calculates subtotal.
  - Used by all public methods to ensure consistent response format.

**Key Features:**
- Constructor injection of repositories via `@RequiredArgsConstructor`.
- Stream API for mapping collections (matching ProductService style).
- Manual DTO mapping (no MapStruct/ModelMapper).
- Exception handling follows existing pattern: `RuntimeException` for not-found cases, `IllegalArgumentException` for validation failures.

---

### 7. Controller: OrderController
**File:** `src/main/java/com/fakecommerce/controllers/OrderController.java`

**Completely rewritten** from an empty stub to a fully functional REST controller.

**Endpoints:**

| HTTP Method | Endpoint | Request Body | Response |
|---|---|---|---|
| POST | `/api/v1/orders` | CreateOrderRequestDto | OrderResponseDto |
| GET | `/api/v1/orders/all` | N/A | List<OrderResponseDto> |
| GET | `/api/v1/orders/{id}` | N/A (path param) | OrderResponseDto |
| PUT | `/api/v1/orders/{id}/status` | UpdateOrderStatusRequestDto | OrderResponseDto |
| DELETE | `/api/v1/orders/{id}` | N/A (path param) | void |

**Key Features:**
- Constructor injection of OrderService via `@RequiredArgsConstructor`.
- No ResponseEntity wrappers (returns plain objects/DTOs, Spring handles serialization to 200 OK).
- No @Valid annotations (validation is done in the service layer).
- Comprehensive Javadoc comments for each endpoint.

---

## DTOs Created

### 1. OrderItemRequestDto
**File:** `src/main/java/com/fakecommerce/dtos/OrderItemRequestDto.java`

Represents a single line item in a create-order request.
- `Long productId`: The product being ordered.
- `Integer quantity`: How many units to order.

### 2. CreateOrderRequestDto
**File:** `src/main/java/com/fakecommerce/dtos/CreateOrderRequestDto.java`

Request body for creating a new order.
- `List<OrderItemRequestDto> items`: The line items to order.

### 3. OrderItemResponseDto
**File:** `src/main/java/com/fakecommerce/dtos/OrderItemResponseDto.java`

Represents a single line item in an order response.
- `Long productId`: The product in this line.
- `String productTitle`: The product name (denormalized for convenience).
- `BigDecimal price`: The unit price.
- `Integer quantity`: Quantity ordered.
- `BigDecimal subtotal`: price × quantity (calculated).

### 4. OrderResponseDto
**File:** `src/main/java/com/fakecommerce/dtos/OrderResponseDto.java`

Complete order representation in responses.
- `Long id`: Order ID.
- `OrderStatus status`: Current status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED).
- `List<OrderItemResponseDto> items`: All line items.
- `BigDecimal totalAmount`: Total order amount.

### 5. UpdateOrderStatusRequestDto
**File:** `src/main/java/com/fakecommerce/dtos/UpdateOrderStatusRequestDto.java`

Request body for updating order status.
- `OrderStatus status`: The new status.

---

## Repository Created

### OrderRepository
**File:** `src/main/java/com/fakecommerce/repository/OrderRepository.java`

Standard JpaRepository for Order entities. Provides basic CRUD operations.

---

## Code Conventions Followed

1. **Dependency Injection:** Constructor injection via Lombok's `@RequiredArgsConstructor` (not field injection with `@Autowired`).
2. **Annotations:** All classes use Lombok annotations (`@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`, etc.).
3. **DTO Mapping:** Manual mapping using Lombok `@Builder` and Java Streams (no MapStruct/ModelMapper).
4. **Exception Handling:**
   - `RuntimeException` with descriptive message for resource-not-found cases.
   - `IllegalArgumentException` for validation failures (e.g., invalid quantity).
5. **Return Types:** Plain objects/DTOs, no `ResponseEntity` wrappers (Spring handles 200 OK serialization).
6. **Validation:** No Bean Validation annotations (`@NotNull`, `@Valid`, etc.) — project doesn't use this dependency.
7. **Comments:** Comprehensive Javadoc-style block comments on methods, especially in service and controller layers.
8. **Soft Deletes:** Orders are soft-deleted via `@SQLDelete` annotation (matches existing entity pattern).

---

## Testing Recommendations

### Manual Testing

1. **Create Order**
   ```bash
   POST /api/v1/orders
   {
     "items": [
       { "productId": 1, "quantity": 2 },
       { "productId": 3, "quantity": 1 }
     ]
   }
   ```
   Expected Response:
   ```json
   {
     "id": 1,
     "status": "PENDING",
     "items": [
       {
         "productId": 1,
         "productTitle": "Product A",
         "price": 10.00,
         "quantity": 2,
         "subtotal": 20.00
       },
       {
         "productId": 3,
         "productTitle": "Product C",
         "price": 15.00,
         "quantity": 1,
         "subtotal": 15.00
       }
     ],
     "totalAmount": 35.00
   }
   ```

2. **Get All Orders**
   ```bash
   GET /api/v1/orders/all
   ```
   Returns a list of all orders in the same format as above.

3. **Get Order by ID**
   ```bash
   GET /api/v1/orders/1
   ```
   Returns the OrderResponseDto for order ID 1.

4. **Update Order Status**
   ```bash
   PUT /api/v1/orders/1/status
   {
     "status": "PROCESSING"
   }
   ```
   Updates the order status and returns the updated OrderResponseDto.

5. **Delete Order**
   ```bash
   DELETE /api/v1/orders/1
   ```
   Soft-deletes the order (HTTP 200 OK, no body).

---

## Important Notes & Known Issues

### 1. Flyway Migration - Ratings Column Conversion
When you first run the application, Flyway will execute V2. **If your local dev database has any non-numeric text in the products.ratings column, the MODIFY COLUMN will fail.** Solutions:
- Clean the existing ratings data to numeric values before running.
- Wipe the local dev database and let Flyway recreate it.

### 2. Pre-existing Bug: OrderItems Soft-Delete
There is a pre-existing bug in `OrderItems.java`'s `@SQLDelete` annotation:
```java
@SQLDelete(sql = "UPDATE order_items SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
```
The SQL targets a nonexistent table `order_items` when the actual table is `order_products`. This means soft-deleting an OrderItems row will silently fail. This bug is out of scope for this implementation but should be fixed separately:
```java
@SQLDelete(sql = "UPDATE order_products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
```

### 3. No User/Customer Linkage
The Order entity currently has **no relationship to any User or Customer entity** because neither exists in the project yet. If you need to track which customer placed an order, you will need to:
1. Create a User/Customer entity.
2. Add authentication/security to the project.
3. Modify the Order entity to include a `userId` or `customerId` field.
4. Update the Flyway migrations and the Order API to capture this information.

### 4. Total Amount Computation
The `totalAmount` is **persisted in the database** (not computed on-the-fly). This means:
- At order creation time, the service calculates and stores it.
- At retrieval time, the stored value is returned (no recalculation).
- If product prices change after an order is placed, the order's totalAmount reflects the price at the time of order creation (immutable).

---

## Build & Deployment

### Compilation
The project has been verified to compile successfully:
```bash
./gradlew compileJava
```

### Running the Application
```bash
./gradlew bootRun
```

### Build & Package
```bash
./gradlew build
```

---

## Summary

The Order API is now fully implemented following the fakecommerce project's established patterns:
- ✅ Database schema updated (Flyway migration).
- ✅ Entity models updated (Order with totalAmount, Product ratings as BigDecimal).
- ✅ DTOs created for requests and responses.
- ✅ Repository layer with OrderRepository and updated OrderItemsRepository.
- ✅ Service layer with complete business logic (OrderService).
- ✅ Controller layer with 5 REST endpoints (OrderController).
- ✅ Comprehensive comments and documentation.
- ✅ No ResponseEntity usage (returns plain objects as requested).
- ✅ Code style matches existing project conventions.

All files compile successfully. The API is ready for testing and deployment.

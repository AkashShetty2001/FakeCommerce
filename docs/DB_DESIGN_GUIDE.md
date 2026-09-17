# FakeCommerce Database Design Guide

## 📋 Table of Contents
1. [Overview](#overview)
2. [Database Architecture](#database-architecture)
3. [Entity Relationship Diagram](#entity-relationship-diagram)
4. [Tables & Columns](#tables--columns)
5. [Relationships](#relationships)
6. [Design Patterns](#design-patterns)
7. [Order Processing Flow](#order-processing-flow)
8. [Data Flow Examples](#data-flow-examples)

---

## Overview

The FakeCommerce database is designed to manage an e-commerce platform with the following core entities:
- **Orders**: Main transaction records
- **OrderItems**: Line items within orders (products in an order)
- **Products**: Product catalog information
- **Categories**: Product categorization system

### Design Philosophy
- **Soft Delete Pattern**: Entities are marked as deleted but not removed from the database
- **Audit Trail**: Automatic tracking of creation and modification times
- **Lazy Loading**: Relationships use lazy loading to optimize performance
- **Inheritance**: Base entity class to reduce code duplication

---

## Database Architecture

### Entity Inheritance Hierarchy

```
┌─────────────────────────────────────────┐
│         BaseEntity (Abstract)           │
│                                         │
│  + id (Primary Key)                     │
│  + created_at (Timestamp)               │
│  + updated_at (Timestamp)               │
│  + deleted_at (Soft Delete Flag)        │
│                                         │
│  @MappedSuperclass                      │
│  @EntityListeners                       │
└──────────────┬──────────────────────────┘
               │
     ┌─────────┼─────────┬──────────┐
     │         │         │          │
     ▼         ▼         ▼          ▼
┌────────┐ ┌────────┐ ┌──────┐ ┌──────────┐
│ Order  │ │OrderI- │ │Produ-│ │Category  │
│        │ │ tems   │ │ ct   │ │          │
└────────┘ └────────┘ └──────┘ └──────────┘
```

---

## Entity Relationship Diagram

```
                    ┌──────────────────────────────────────────┐
                    │          CATEGORIES TABLE               │
                    │────────────────────────────────────────│
                    │ id (PK)                                  │
                    │ category_name (VARCHAR)    NOT NULL      │
                    │ created_at (TIMESTAMP)     NOT NULL      │
                    │ updated_at (TIMESTAMP)                   │
                    │ deleted_at (TIMESTAMP)     [Soft Delete] │
                    └──────────────┬───────────────────────────┘
                                   │
                                   │ 1:N
                                   │ (foreign key: category_id)
                                   │
                    ┌──────────────▼───────────────────────────┐
                    │          PRODUCTS TABLE                 │
                    │────────────────────────────────────────│
                    │ id (PK)                                  │
                    │ title (VARCHAR)            NOT NULL      │
                    │ price (DECIMAL)            NOT NULL      │
                    │ image (VARCHAR)                          │
                    │ category_id (FK)           NOT NULL      │
                    │ ratings (DECIMAL)                        │
                    │ description (TEXT)                       │
                    │ created_at (TIMESTAMP)     NOT NULL      │
                    │ updated_at (TIMESTAMP)                   │
                    │ deleted_at (TIMESTAMP)     [Soft Delete] │
                    └──────────────┬───────────────────────────┘
                                   │
                                   │ 1:N
                                   │ (foreign key: product_id)
                                   │
        ┌──────────────────────────┴──────────────────────────┐
        │                                                      │
        │                                                      │
┌───────▼──────────────────────┐      ┌─────────────────────┐
│   ORDER_ITEMS TABLE          │      │   ORDERS TABLE      │
│(Composite OrderItems Entity) │      │─────────────────────│
│──────────────────────────────│      │ id (PK)             │
│ id (PK)                      │      │ status (ENUM)   ◄──┐
│ order_id (FK)        NOT NULL│──────┤ total_amount (DEC) │
│ product_id (FK)      NOT NULL│──┐   │ created_at (TS)    │
│ quantity (INT)                │  └──┤ updated_at (TS)    │
│ created_at (TIMESTAMP)        │     │ deleted_at (TS)    │
│ updated_at (TIMESTAMP)        │     │[Soft Delete]       │
│ deleted_at (TIMESTAMP)        │     └─────────────────────┘
│[Soft Delete]                  │
└───────────────────────────────┘

Key: PK = Primary Key, FK = Foreign Key, TS = Timestamp
```

---

## Tables & Columns

### 1. CATEGORIES Table
**Purpose**: Store product categories

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `category_name` | VARCHAR(255) | NOT NULL | Category name (e.g., "Electronics", "Clothing") |
| `created_at` | TIMESTAMP | NOT NULL | Record creation time (auto-set) |
| `updated_at` | TIMESTAMP | NULL | Last modification time (auto-updated) |
| `deleted_at` | TIMESTAMP | NULL | Soft delete marker |

**Example Data**:
```
id | category_name | created_at | updated_at | deleted_at
1  | Electronics   | 2026-01-01 | 2026-01-01 | NULL
2  | Clothing      | 2026-01-02 | 2026-01-02 | NULL
3  | Books         | 2026-01-03 | 2026-01-03 | NULL
```

---

### 2. PRODUCTS Table
**Purpose**: Store product information

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `title` | VARCHAR(255) | NOT NULL | Product name |
| `price` | DECIMAL(10,2) | NOT NULL | Product price |
| `image` | VARCHAR(255) | NULL | Product image URL |
| `category_id` | BIGINT | NOT NULL, FK | References categories.id |
| `ratings` | DECIMAL(3,2) | NULL | Product rating (e.g., 4.50) |
| `description` | TEXT | NULL | Product description |
| `created_at` | TIMESTAMP | NOT NULL | Record creation time (auto-set) |
| `updated_at` | TIMESTAMP | NULL | Last modification time (auto-updated) |
| `deleted_at` | TIMESTAMP | NULL | Soft delete marker |

**Example Data**:
```
id | title          | price | category_id | ratings | created_at | description
1  | iPhone 15      | 999.99| 1           | 4.80    | 2026-01-01 | Latest Apple smartphone
2  | Samsung TV     | 799.99| 1           | 4.50    | 2026-01-02 | 55-inch 4K Smart TV
3  | Cotton T-Shirt | 29.99 | 2           | 4.20    | 2026-01-03 | 100% organic cotton
```

---

### 3. ORDERS Table
**Purpose**: Store order header information

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique order identifier |
| `status` | VARCHAR(50) | NOT NULL | Order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED) |
| `total_amount` | DECIMAL(15,2) | NULL | Total order amount (sum of all line items) |
| `created_at` | TIMESTAMP | NOT NULL | Order creation time (auto-set) |
| `updated_at` | TIMESTAMP | NULL | Last update time (auto-updated) |
| `deleted_at` | TIMESTAMP | NULL | Soft delete marker |

**Example Data**:
```
id | status     | total_amount | created_at | updated_at | deleted_at
1  | PENDING    | 1999.97      | 2026-01-05 | 2026-01-05 | NULL
2  | SHIPPED    | 599.99       | 2026-01-06 | 2026-01-07 | NULL
3  | DELIVERED  | 29.99        | 2026-01-04 | 2026-01-08 | NULL
```

---

### 4. ORDER_PRODUCTS Table (Order Items)
**Purpose**: Store line items for each order (junction table)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique line item identifier |
| `order_id` | BIGINT | NOT NULL, FK | References orders.id |
| `product_id` | BIGINT | NOT NULL, FK | References products.id |
| `quantity` | INT | NOT NULL | Number of units ordered |
| `created_at` | TIMESTAMP | NOT NULL | Record creation time (auto-set) |
| `updated_at` | TIMESTAMP | NULL | Last modification time (auto-updated) |
| `deleted_at` | TIMESTAMP | NULL | Soft delete marker |

**Example Data**:
```
id | order_id | product_id | quantity | created_at | deleted_at
1  | 1        | 1          | 2        | 2026-01-05 | NULL
2  | 1        | 2          | 1        | 2026-01-05 | NULL
3  | 2        | 3          | 5        | 2026-01-06 | NULL
```

---

## Relationships

### 1. Category ↔ Product (One-to-Many)

```
One Category has Many Products

┌──────────────┐
│  CATEGORY    │
│  id=1        │
│  Electronics │
└───────┬──────┘
        │
        │ 1:N relationship
        │
        ├──► PRODUCT(id=1) - iPhone 15, price=999.99, category_id=1
        ├──► PRODUCT(id=2) - Samsung TV, price=799.99, category_id=1
        └──► PRODUCT(id=n) - ...
```

**Java Implementation**:
```java
// In Category class (if it had the relationship)
@OneToMany(mappedBy = "category")
private List<Product> products;

// In Product class
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id", nullable = false)
private Category category;
```

---

### 2. Product ↔ OrderItems (One-to-Many)

```
One Product appears in Many OrderItems

┌──────────────┐
│  PRODUCT     │
│  id=1        │
│  iPhone 15   │
└───────┬──────┘
        │
        │ 1:N relationship
        │
        ├──► ORDER_ITEM(order_id=1, quantity=2)
        ├──► ORDER_ITEM(order_id=5, quantity=1)
        └──► ORDER_ITEM(order_id=n, quantity=x)
```

**Java Implementation**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "product_id", nullable = false)
private Product product;
```

---

### 3. Order ↔ OrderItems (One-to-Many)

```
One Order has Many OrderItems

┌──────────────────────┐
│  ORDER               │
│  id=1                │
│  status=PENDING      │
│  total_amount=1999.97│
└──────────┬───────────┘
           │
           │ 1:N relationship
           │
           ├──► ORDER_ITEM(product_id=1, quantity=2) - iPhone 15
           ├──► ORDER_ITEM(product_id=2, quantity=1) - Samsung TV
           └──► More items...
```

**Java Implementation**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id", nullable = false)
private Order order;
```

---

## Design Patterns

### 1. **Soft Delete Pattern**

**Concept**: Instead of physically deleting records, mark them with a `deleted_at` timestamp.

```sql
-- Instead of: DELETE FROM orders WHERE id = 1;
-- We do:
UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = 1;

-- When querying, automatically exclude soft-deleted records:
SELECT * FROM orders WHERE deleted_at IS NULL;
```

**Benefits**:
- ✅ Data recovery (can restore deleted records)
- ✅ Audit trail (know when records were deleted)
- ✅ Maintains referential integrity
- ✅ Historical data analysis

**Hibernate Implementation**:
```java
@SQLDelete(sql = "UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Order extends BaseEntity {
    // ...
}
```

---

### 2. **Entity Inheritance (Mapped Superclass)**

**Concept**: Use a base class to share common fields across multiple entities.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

**Classes using BaseEntity**:
- Order
- OrderItems
- Product
- Category

**Benefits**:
- ✅ DRY (Don't Repeat Yourself)
- ✅ Consistent audit fields across all entities
- ✅ Reduces boilerplate code
- ✅ Automatic timestamp management

---

### 3. **Lazy Loading**

**Concept**: Don't load related entities until explicitly accessed.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "product_id", nullable = false)
private Product product;
```

**Benefits**:
- ✅ Better performance (don't load unnecessary data)
- ✅ Reduces memory usage
- ✅ Faster queries
- ⚠️ Risk of LazyInitializationException if session is closed

---

### 4. **Foreign Key Constraints**

All relationships are enforced at the database level:

```sql
-- OrderItems must reference valid orders
ALTER TABLE order_items
ADD CONSTRAINT fk_order_items_order_id
FOREIGN KEY (order_id) REFERENCES orders(id);

-- OrderItems must reference valid products
ALTER TABLE order_items
ADD CONSTRAINT fk_order_items_product_id
FOREIGN KEY (product_id) REFERENCES products(id);

-- Products must reference valid categories
ALTER TABLE products
ADD CONSTRAINT fk_products_category_id
FOREIGN KEY (category_id) REFERENCES categories(id);
```

---

## Order Processing Flow

### Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                       ORDER LIFECYCLE                           │
└─────────────────────────────────────────────────────────────────┘

1. ORDER CREATION
   ┌──────────────────────────────────────────┐
   │ User submits order with items            │
   │ Creates Order (status = PENDING)         │
   │ Creates OrderItems (one per product)     │
   │ Calculates total_amount                  │
   └────────────┬─────────────────────────────┘
                │
                ▼
2. ORDER CONFIRMATION
   ┌──────────────────────────────────────────┐
   │ Order status = PROCESSING                │
   │ Payment processed                        │
   │ Inventory reserved                       │
   └────────────┬─────────────────────────────┘
                │
                ▼
3. ORDER FULFILLMENT
   ┌──────────────────────────────────────────┐
   │ Order status = SHIPPED                   │
   │ Items picked and packed                  │
   │ Tracking info generated                  │
   └────────────┬─────────────────────────────┘
                │
                ▼
4. ORDER DELIVERY
   ┌──────────────────────────────────────────┐
   │ Order status = DELIVERED                 │
   │ Delivery confirmation received           │
   │ Order complete                           │
   └────────────────────────────────────────┘

Alternative: CANCELLATION
   ┌──────────────────────────────────────────┐
   │ Order status = CANCELLED                 │
   │ Payment refunded                         │
   │ Inventory returned                       │
   └────────────────────────────────────────┘
```

### OrderStatus Enum

```java
public enum OrderStatus {
    PENDING,      // Initial state when order is created
    PROCESSING,   // Payment verified, preparing to ship
    SHIPPED,      // Order sent to customer
    DELIVERED,    // Order reached customer
    CANCELLED     // Order was cancelled by user/admin
}
```

---

## Data Flow Examples

### Example 1: Creating an Order

```
INPUT (from API):
{
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 2, "quantity": 1}
  ]
}

PROCESS:
1. Create Order record
   - id: 1
   - status: PENDING
   - created_at: 2026-01-05 10:30:00
   - total_amount: NULL (calculated after items added)

2. Create OrderItems
   ┌─────────────────────────────────┐
   │ OrderItem 1                     │
   │ order_id: 1                     │
   │ product_id: 1 (iPhone 15)       │
   │ quantity: 2                     │
   │ Price per unit: 999.99          │
   │ Line total: 1999.98             │
   └─────────────────────────────────┘

   ┌─────────────────────────────────┐
   │ OrderItem 2                     │
   │ order_id: 1                     │
   │ product_id: 2 (Samsung TV)      │
   │ quantity: 1                     │
   │ Price per unit: 799.99          │
   │ Line total: 799.99              │
   └─────────────────────────────────┘

3. Calculate total_amount
   - Total = 1999.98 + 799.99 = 2799.97
   - Update Order.total_amount = 2799.97

DATABASE STATE:

orders table:
┌────┬──────────┬──────────────┐
│ id │ status   │ total_amount │
├────┼──────────┼──────────────┤
│ 1  │ PENDING  │ 2799.97      │
└────┴──────────┴──────────────┘

order_items table:
┌────┬──────────┬────────────┬──────────┐
│ id │ order_id │ product_id │ quantity │
├────┼──────────┼────────────┼──────────┤
│ 1  │ 1        │ 1          │ 2        │
│ 2  │ 1        │ 2          │ 1        │
└────┴──────────┴────────────┴──────────┘
```

---

### Example 2: Querying an Order with All Related Data

```sql
-- SQL Query to get complete order information
SELECT 
    o.id AS order_id,
    o.status,
    o.total_amount,
    oi.product_id,
    p.title AS product_title,
    p.price AS unit_price,
    oi.quantity,
    (p.price * oi.quantity) AS line_total,
    c.category_name
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
LEFT JOIN products p ON oi.product_id = p.id
LEFT JOIN categories c ON p.category_id = c.id
WHERE o.id = 1
  AND o.deleted_at IS NULL
  AND oi.deleted_at IS NULL
  AND p.deleted_at IS NULL;

RESULT:
┌──────────┬─────────┬──────────────┬────────────┬──────────────────┬───────────┬──────────┬────────────┐
│ order_id │ status  │ total_amount │ product_id │ product_title    │ unit_price│ quantity │ line_total │
├──────────┼─────────┼──────────────┼────────────┼──────────────────┼───────────┼──────────┼────────────┤
│ 1        │ PENDING │ 2799.97      │ 1          │ iPhone 15        │ 999.99    │ 2        │ 1999.98    │
│ 1        │ PENDING │ 2799.97      │ 2          │ Samsung TV       │ 799.99    │ 1        │ 799.99     │
└──────────┴─────────┴──────────────┴────────────┴──────────────────┴───────────┴──────────┴────────────┘
```

---

### Example 3: Soft Delete in Action

```
BEFORE DELETE:
orders table:
┌────┬──────────┬──────────────┬────────────┐
│ id │ status   │ total_amount │ deleted_at │
├────┼──────────┼──────────────┼────────────┤
│ 1  │ PENDING  │ 2799.97      │ NULL       │
│ 2  │ SHIPPED  │ 599.99       │ NULL       │
└────┴──────────┴──────────────┴────────────┘

DELETE OPERATION:
DELETE FROM orders WHERE id = 1;

ACTUAL DATABASE EXECUTION (due to @SQLDelete annotation):
UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = 1;
-- deleted_at = 2026-01-10 15:30:00

AFTER DELETE:
orders table (physical state):
┌────┬──────────┬──────────────┬─────────────────────────┐
│ id │ status   │ total_amount │ deleted_at              │
├────┼──────────┼──────────────┼─────────────────────────┤
│ 1  │ PENDING  │ 2799.97      │ 2026-01-10 15:30:00     │
│ 2  │ SHIPPED  │ 599.99       │ NULL                    │
└────┴──────────┴──────────────┴─────────────────────────┘

WHEN QUERYING (due to @SQLRestriction annotation):
SELECT * FROM orders;
-- Automatically adds: WHERE deleted_at IS NULL

QUERY RESULT (logical view):
┌────┬──────────┬──────────────┐
│ id │ status   │ total_amount │
├────┼──────────┼──────────────┤
│ 2  │ SHIPPED  │ 599.99       │
└────┴──────────┴──────────────┘

RECOVERY:
-- To recover deleted order:
UPDATE orders SET deleted_at = NULL WHERE id = 1;
-- Order 1 becomes visible again in queries
```

---

## Database Constraints & Rules

### Primary Keys
- All tables have a `BIGINT id` as primary key with auto-increment
- Ensures unique identification of each record

### Foreign Keys
| Table | FK Column | References | Constraint |
|-------|-----------|-----------|------------|
| products | category_id | categories.id | NOT NULL |
| order_items | order_id | orders.id | NOT NULL |
| order_items | product_id | products.id | NOT NULL |

### Null Rules
| Column | Null Allowed | Reason |
|--------|--------------|--------|
| created_at | NO | Essential for audit trail |
| updated_at | YES | Set only on modification |
| deleted_at | YES | NULL = not deleted |
| total_amount (Order) | YES | Calculated, can be NULL initially |
| category_id (Product) | NO | Every product must have a category |

---

## Indexes for Performance

**Recommended Indexes** (for optimal query performance):

```sql
-- Soft delete filtering (heavily used in WHERE clauses)
CREATE INDEX idx_orders_deleted_at ON orders(deleted_at);
CREATE INDEX idx_order_items_deleted_at ON order_items(deleted_at);
CREATE INDEX idx_products_deleted_at ON products(deleted_at);
CREATE INDEX idx_categories_deleted_at ON categories(deleted_at);

-- Foreign key lookups
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_products_category_id ON products(category_id);

-- Order status queries
CREATE INDEX idx_orders_status ON orders(status);

-- Timestamp-based queries
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_products_ratings ON products(ratings);
```

---

## Common Queries

### Get All Orders with Items
```sql
SELECT 
    o.*,
    oi.id as item_id,
    oi.product_id,
    oi.quantity,
    p.title,
    p.price
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id AND oi.deleted_at IS NULL
LEFT JOIN products p ON oi.product_id = p.id AND p.deleted_at IS NULL
WHERE o.deleted_at IS NULL;
```

### Orders by Status
```sql
SELECT COUNT(*) as count, status
FROM orders
WHERE deleted_at IS NULL
GROUP BY status;
```

### Product Revenue
```sql
SELECT 
    p.id,
    p.title,
    COUNT(oi.id) as times_ordered,
    SUM(oi.quantity) as total_sold,
    SUM(oi.quantity * p.price) as revenue
FROM products p
LEFT JOIN order_items oi ON p.id = oi.product_id 
    AND oi.deleted_at IS NULL
LEFT JOIN orders o ON oi.order_id = o.id 
    AND o.status = 'DELIVERED'
    AND o.deleted_at IS NULL
WHERE p.deleted_at IS NULL
GROUP BY p.id, p.title;
```

---

## Summary

| Aspect | Details |
|--------|---------|
| **Total Tables** | 4 (categories, products, orders, order_items) |
| **Total Relationships** | 3 (Category→Product, Product→OrderItem, Order→OrderItem) |
| **Key Design Pattern** | Soft Delete (logical delete) |
| **Inheritance** | BaseEntity with audit fields |
| **Data Loading** | Lazy loading for relationships |
| **Uniqueness** | All entities identified by unique BIGINT id |
| **Audit Trail** | created_at, updated_at on every entity |
| **Soft Delete** | deleted_at timestamp field |

---

## Migration History

The database is managed through **Flyway migrations**. Key migrations:
- **V1**: Initial schema (tables and relationships)
- **V2**: Added `total_amount` to orders, changed ratings type to DECIMAL

---

## Best Practices

1. **Always check `deleted_at IS NULL`** in queries to respect soft deletes
2. **Use lazy loading** to avoid N+1 query problems
3. **Calculate order totals** after all items are added
4. **Maintain referential integrity** through foreign keys
5. **Use transactions** when creating orders with items
6. **Add appropriate indexes** for frequently queried columns
7. **Archive old data** periodically to maintain performance

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-14  
**Database Type**: SQL (MySQL/PostgreSQL compatible)  
**ORM**: Hibernate/Spring Data JPA

# Low-Level Design: Order & Product Reviews

> **Status:** Design only. Nothing in this document has been implemented —
> it is meant to be built by hand, one layer at a time, using the existing
> Category/Product/Order code as the reference pattern (entity → repository
> → DTO → mapper → service → controller → exception → docs). Treat the
> `IMPLEMENTATION CHECKLIST` at the bottom as the suggested build order.

## 1. Goal

Let a customer leave feedback in two related but distinct places:

1. **Order review** — one review for the overall order experience
   (delivery, packaging, service).
2. **Product review** — a review for each individual product that was
   part of a delivered order, tied to the specific line item that was
   purchased.

This mirrors what most e-commerce platforms do: you review "the delivery"
once, and separately rate each item you bought.

### Out of scope (MVP)

- Review moderation / approval workflow
- Uploading images with a review
- Replies, helpful/unhelpful votes
- A real user/auth system (the project has none yet — see §3)

---

## 2. Where this fits in the existing schema

```
Category ──1:N── Product ──1:N── OrderItems ──N:1── Order
                                     │
                                     │ 1:1 (new)
                                     ▼
                              ProductReview (new)

Order ──1:1── OrderReview (new)
```

- `OrderReview` attaches to `Order` (one review per order).
- `ProductReview` attaches to `OrderItems`, **not** directly to `Product`.
  Anchoring to the order item (rather than just `product_id`) is what
  proves the reviewer actually bought that product in that order, and
  naturally supports "review each product in the order separately" even
  if the same product appears in two different orders.

---

## 3. Business rules

| Rule | Reasoning |
|------|-----------|
| A review can only be created once `Order.status == DELIVERED` | You shouldn't be able to review something you haven't received yet. |
| At most one `OrderReview` per order | Enforced with a unique constraint on `order_id`. |
| At most one `ProductReview` per order item | Enforced with a unique constraint on `order_item_id`. You can still leave a separate review each time you buy the product again in a new order. |
| Rating is an integer 1–5 | Standard star-rating scale; keep it simple, no half-stars for MVP. |
| No `user_id` on either entity | The project has no auth/User entity yet (same call already made for `Order` — see `IMPLEMENTATION_CHECKLIST.md` Requirement 2). If/when auth is added, add a nullable `user_id` column via a follow-up migration rather than blocking this feature on it. |
| Comment is optional, rating is required | A star rating with no text is still a valid review. |

---

## 4. Entities

### 4.1 `OrderReview`

```java
package com.fakecommerce.schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_reviews")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE order_reviews SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OrderReview extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comment;
}
```

### 4.2 `ProductReview`

```java
package com.fakecommerce.schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_reviews")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE product_reviews SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ProductReview extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItems orderItem;

    // Denormalized for simple "all reviews for this product" queries
    // without joining through order_items every time.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comment;
}
```

Both follow the existing conventions already used across `Order`,
`OrderItems`, `Product`, `Category`: extend `BaseEntity` (id + audit
timestamps), soft delete via `@SQLDelete` / `@SQLRestriction`, Lombok
`@Data`/`@Builder`.

---

## 5. Database migration

Add as `src/main/resources/db/migration/V3__add_order_and_product_reviews.sql`
(the project is already on V2 — see `docs/DB_DESIGN_GUIDE.md` migration
history):

```sql
CREATE TABLE order_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_order_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT uq_order_reviews_order UNIQUE (order_id),
    CONSTRAINT chk_order_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE product_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_product_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_products(id),
    CONSTRAINT fk_product_reviews_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_product_reviews_order_item UNIQUE (order_item_id),
    CONSTRAINT chk_product_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_product_reviews_product_id ON product_reviews(product_id);
CREATE INDEX idx_order_reviews_deleted_at ON order_reviews(deleted_at);
CREATE INDEX idx_product_reviews_deleted_at ON product_reviews(deleted_at);
```

Note: `order_items` maps to table `order_products` (see `OrderItems.java`
`@Table(name = "order_products")`) — the FK above targets that actual
table name, not the entity name.

---

## 6. DTOs

Following the same request/response DTO split already used for
Category/Product/Order, plus a MapStruct mapper per entity
(`docs/MAPSTRUCT_DTO_MAPPING_GUIDE.md`):

```java
// dtos/CreateOrderReviewRequestDto.java
public class CreateOrderReviewRequestDto {
    private Integer rating;
    private String comment;
}

// dtos/OrderReviewResponseDto.java
public class OrderReviewResponseDto {
    private Long id;
    private Long orderId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

// dtos/CreateProductReviewRequestDto.java
public class CreateProductReviewRequestDto {
    private Integer rating;
    private String comment;
}

// dtos/ProductReviewResponseDto.java
public class ProductReviewResponseDto {
    private Long id;
    private Long orderItemId;
    private Long productId;
    private String productTitle;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
```

`OrderMapper`/`ProductMapper` already exist (see
`src/main/java/com/fakecommerce/mappers/`) — add sibling
`OrderReviewMapper` and `ProductReviewMapper` interfaces the same way,
rather than folding review mapping into the existing mappers.

---

## 7. API endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/v1/orders/{orderId}/review` | Create the order-level review |
| `GET` | `/api/v1/orders/{orderId}/review` | Get the order-level review (404 if none yet) |
| `POST` | `/api/v1/orders/{orderId}/items/{orderItemId}/review` | Create a review for one product in the order |
| `GET` | `/api/v1/orders/{orderId}/items/{orderItemId}/review` | Get that product review |
| `GET` | `/api/v1/product/{productId}/reviews` | Public-facing: list all reviews for a product, across all orders |

Response bodies wrap in the existing `ApiResponse<T>` envelope, same as
every other controller in the project. `PUT`/`DELETE` for editing or
retracting a review are a reasonable v2 addition but are left out of the
MVP scope here.

---

## 8. Service layer flow

### 8.1 Create order review

```
1. Fetch Order by orderId -> 404 ResourceNotFoundException if missing
2. If order.status != DELIVERED -> 409/400 OrderNotReviewableException
3. If an OrderReview already exists for this order -> 409 DuplicateReviewException
4. Validate rating is between 1 and 5
5. Save OrderReview
6. Return OrderReviewResponseDto
```

### 8.2 Create product review

```
1. Fetch Order by orderId -> 404 if missing
2. If order.status != DELIVERED -> 409/400 OrderNotReviewableException
3. Fetch OrderItems by orderItemId, and confirm orderItem.order.id == orderId
   -> 404 if missing, 400 if it belongs to a different order
4. If a ProductReview already exists for this order item -> 409 DuplicateReviewException
5. Validate rating is between 1 and 5
6. Save ProductReview (product_id copied from orderItem.getProduct())
7. Recompute Product.ratings for orderItem.getProduct() (see §9)
8. Return ProductReviewResponseDto
```

New exceptions needed, following the existing pattern in
`src/main/java/com/fakecommerce/exceptions/`
(`CategoryNotFoundException`, `ResourceNotFoundException`):

- `OrderNotReviewableException` — order isn't `DELIVERED` yet
- `DuplicateReviewException` — a review already exists for that order / order item

Both should be registered in `GlobalExceptionHandler` alongside the
existing handlers, mapped to `409 CONFLICT` (duplicate) and
`400 BAD_REQUEST` (not yet reviewable).

---

## 9. Keeping `Product.ratings` in sync

`Product.ratings` currently is a value set directly on creation/update
(see `CreateProductRequestDto.ratings`). Once reviews exist, it should
become the **average of all ProductReview ratings for that product**
instead of an arbitrary input value.

Simplest approach for this scale of app — recompute eagerly, in the same
transaction, right after a `ProductReview` is saved:

```java
BigDecimal newAverage = productReviewRepository.findAverageRatingByProductId(productId);
Product product = productRepository.findById(productId).orElseThrow(...);
product.setRatings(newAverage);
productRepository.save(product);
```

with a repository query such as:

```java
@Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId")
BigDecimal findAverageRatingByProductId(Long productId);
```

This keeps `GET /api/v1/product/{id}` accurate without a scheduled job.
At larger scale this would move to an async/batch recompute, but that's
unnecessary here.

Once this lands, `CreateProductRequestDto.ratings` / `ratings` on product
create-update should arguably be removed in favor of it always being
derived — flagging that as a follow-up decision rather than deciding it
here, since it changes an existing endpoint's contract.

---

## 10. Sequence: creating a product review

```
Client                Controller            Service                  Repositories
  │  POST .../items/{id}/review │                        │
  ├─────────────────────────────►│                        │
  │                              │  createProductReview()  │
  │                              ├───────────────────────►│
  │                              │                         │ findOrderById
  │                              │                         ├─────────────►│
  │                              │                         │◄──────status DELIVERED?
  │                              │                         │
  │                              │                         │ findOrderItemById
  │                              │                         ├─────────────►│
  │                              │                         │◄──────belongs to order?
  │                              │                         │
  │                              │                         │ existsByOrderItemId
  │                              │                         ├─────────────►│
  │                              │                         │◄──────already reviewed?
  │                              │                         │
  │                              │                         │ save(ProductReview)
  │                              │                         ├─────────────►│
  │                              │                         │
  │                              │                         │ recompute Product.ratings
  │                              │                         ├─────────────►│
  │                              │◄────ProductReviewResponseDto           │
  │◄─────────ApiResponse<...>────┤                         │
```

---

## 11. Implementation checklist (suggested build order)

Following the same order the Order API and the MapStruct guide were
built in:

1. [ ] `V3__add_order_and_product_reviews.sql` migration
2. [ ] `schema/OrderReview.java`, `schema/ProductReview.java`
3. [ ] `repository/OrderReviewRepository.java`, `repository/ProductReviewRepository.java` (+ `findAverageRatingByProductId`)
4. [ ] `dtos/CreateOrderReviewRequestDto.java`, `OrderReviewResponseDto.java`, `CreateProductReviewRequestDto.java`, `ProductReviewResponseDto.java`
5. [ ] `mappers/OrderReviewMapper.java`, `mappers/ProductReviewMapper.java` (MapStruct, `componentModel = "spring"`, same shape as `CategoryMapper`)
6. [ ] `exceptions/OrderNotReviewableException.java`, `exceptions/DuplicateReviewException.java` + wire into `GlobalExceptionHandler`
7. [ ] `services/OrderReviewService.java`, `services/ProductReviewService.java` (or extend `OrderService`/`ProductService` — your call)
8. [ ] `controllers/` endpoints from §7, with the same `ResponseEntity<ApiResponse<T>>` + Javadoc-per-endpoint style used in `OrderController`
9. [ ] Manual test pass: try reviewing a `PENDING` order (should fail), review a `DELIVERED` order twice (should fail second time), confirm `Product.ratings` updates after a product review

---

## 12. Open questions for whoever implements this

- Should `OrderReview`/`ProductReview` support edits (`PUT`) and
  retraction (`DELETE`), or is create-only enough for v1?
- When auth eventually exists, does a review need to prove *who* bought
  it (via `user_id` on `Order`), and should old anonymous reviews be
  backfilled or left as-is?
- Is a 1–5 integer scale sufficient, or is half-star precision needed later?

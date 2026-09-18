# Product API - Quick Reference

## Base URL
```
http://localhost:8080/api/v1/product
```

---

## 1. Get All Products
**Request:**
```
GET /api/v1/product/all
```

**Response:** (HTTP 200)
```json
[
  {
    "id": 1,
    "title": "Product A",
    "price": 10.00,
    "image": "https://example.com/product-a.png",
    "ratings": 4.5,
    "description": "A great product."
  },
  {
    "id": 2,
    "title": "Product B",
    "price": 25.00,
    "image": "https://example.com/product-b.png",
    "ratings": 3.8,
    "description": "Another great product."
  }
]
```

---

## 2. Get Product by ID
**Request:**
```
GET /api/v1/product/{id}
```

**Example:**
```
GET /api/v1/product/1
```

**Response:** (HTTP 200)
```json
{
  "id": 1,
  "title": "Product A",
  "price": 10.00,
  "image": null,
  "ratings": 4.5,
  "description": "A great product."
}
```

**Error Responses:**
- `500` (RuntimeException): Product not found

---

## 3. Get Product with Details by ID
**Request:**
```
GET /api/v1/product/{id}/details
```

**Example:**
```
GET /api/v1/product/1/details
```

**Response:** (HTTP 200)
```json
{
  "id": 1,
  "title": "Product A",
  "price": 10.00,
  "image": "https://example.com/product-a.png",
  "ratings": 4.5,
  "description": "A great product.",
  "category": {
    "id": 1,
    "categoryName": "Electronics",
    "createdAt": "2026-01-01T10:00:00",
    "updatedAt": null,
    "deletedAt": null
  }
}
```

**Error Responses:**
- `500` (RuntimeException): Product not found

---

## 4. Create Product
**Request:**
```
POST /api/v1/product
Content-Type: application/json

{
  "title": "Product A",
  "price": 10.00,
  "image": "https://example.com/product-a.png",
  "categoryId": 1,
  "ratings": 4.5,
  "description": "A great product."
}
```

**Response:** (HTTP 200)
```json
{
  "id": 1,
  "title": "Product A",
  "price": 10.00,
  "image": "https://example.com/product-a.png",
  "category": {
    "id": 1,
    "categoryName": "Electronics",
    "createdAt": "2026-01-01T10:00:00",
    "updatedAt": null,
    "deletedAt": null
  },
  "ratings": 4.5,
  "description": "A great product.",
  "createdAt": "2026-09-18T09:00:00",
  "updatedAt": null,
  "deletedAt": null
}
```

**Note:** Returns the full `Product` entity (not a response DTO), so it includes the nested `category` object and audit fields.

**Error Responses:**
- `500` (RuntimeException): Category not found

---

## 5. Update Product
**Request:**
```
PUT /api/v1/product/{id}
Content-Type: application/json

{
  "title": "Product A (Updated)",
  "price": 12.00,
  "image": "https://example.com/product-a-v2.png",
  "categoryId": 2,
  "ratings": 4.7,
  "description": "An even better product."
}
```

**Example:**
```
PUT /api/v1/product/1
{
  "price": 15.00
}
```

**Response:** (HTTP 200)
```json
{
  "id": 1,
  "title": "Product A (Updated)",
  "price": 12.00,
  "image": "https://example.com/product-a-v2.png",
  "category": {
    "id": 2,
    "categoryName": "Home & Kitchen",
    "createdAt": "2026-01-01T10:00:00",
    "updatedAt": null,
    "deletedAt": null
  },
  "ratings": 4.7,
  "description": "An even better product.",
  "createdAt": "2026-09-18T09:00:00",
  "updatedAt": "2026-09-18T09:15:00",
  "deletedAt": null
}
```

**Note:** Partial update — only non-null fields in the request body are applied. Any field left out of the request keeps its existing value.

**Error Responses:**
- `500` (RuntimeException): Product not found
- `500` (RuntimeException): Category not found (if `categoryId` is provided but invalid)

---

## 6. Delete Product
**Request:**
```
DELETE /api/v1/product/{id}
```

**Example:**
```
DELETE /api/v1/product/1
```

**Response:** (HTTP 200)
```
(empty body)
```

**Note:** Delete is soft-delete. The product is marked as deleted (`deleted_at` set) in the database but not physically removed.

**Error Responses:**
- `500` (IllegalArgumentException): Product does not exist

---

## 7. Get Products by Category
**Request:**
```
GET /api/v1/product/search?categoryName={categoryName}
```

**Example:**
```
GET /api/v1/product/search?categoryName=Electronics
```

**Response:** (HTTP 200)
```json
[
  {
    "id": 1,
    "title": "Product A",
    "price": 10.00,
    "image": "https://example.com/product-a.png",
    "category": {
      "id": 1,
      "categoryName": "Electronics",
      "createdAt": "2026-01-01T10:00:00",
      "updatedAt": null,
      "deletedAt": null
    },
    "ratings": 4.5,
    "description": "A great product.",
    "createdAt": "2026-09-18T09:00:00",
    "updatedAt": null,
    "deletedAt": null
  }
]
```

**Note:** Returns full `Product` entities (not response DTOs). Matching is against the category's `categoryName`.

---

## 8. Get Distinct Categories
**Request:**
```
GET /api/v1/product/categories
```

**Response:** (HTTP 200)
```json
[
  "Electronics",
  "Home & Kitchen",
  "Books"
]
```

---

## DTO Structures

### CreateProductRequestDto
_(also used as the request body for update)_
```json
{
  "title": "<string>",
  "price": <decimal>,
  "image": "<string>",
  "categoryId": <number>,
  "ratings": <decimal>,
  "description": "<string>"
}
```

### GetProductResponseDto
```json
{
  "id": <number>,
  "title": "<string>",
  "price": <decimal>,
  "image": "<string>",
  "ratings": <decimal>,
  "description": "<string>"
}
```

### GetProductWithDetailsDto
_(extends `GetProductResponseDto`)_
```json
{
  "id": <number>,
  "title": "<string>",
  "price": <decimal>,
  "image": "<string>",
  "ratings": <decimal>,
  "description": "<string>",
  "category": {
    "id": <number>,
    "categoryName": "<string>",
    "createdAt": "<datetime>",
    "updatedAt": "<datetime>",
    "deletedAt": "<datetime>"
  }
}
```

---

## Field Validation Rules

### CreateProductRequestDto
- `title`: Should be provided (entity column is `nullable = false`)
- `price`: Should be provided (entity column is `nullable = false`)
- `categoryId`: Must reference an existing category
- `image`, `ratings`, `description`: Optional

### Update (same DTO, partial semantics)
- Any field left `null` in the request body is left unchanged on the existing product.
- `categoryId`, if provided, must reference an existing category.

---

## Common HTTP Status Codes

| Code | Description |
|------|---|
| 200 | Success (all operations) |
| 500 | Server error (product/category not found, other exceptions) |

---

## Example cURL Commands

### Get All Products
```bash
curl http://localhost:8080/api/v1/product/all
```

### Get Product by ID
```bash
curl http://localhost:8080/api/v1/product/1
```

### Get Product with Details by ID
```bash
curl http://localhost:8080/api/v1/product/1/details
```

### Create Product
```bash
curl -X POST http://localhost:8080/api/v1/product \
  -H "Content-Type: application/json" \
  -d '{"title":"Product A","price":10.00,"image":"https://example.com/product-a.png","categoryId":1,"ratings":4.5,"description":"A great product."}'
```

### Update Product
```bash
curl -X PUT http://localhost:8080/api/v1/product/1 \
  -H "Content-Type: application/json" \
  -d '{"price":15.00}'
```

### Delete Product
```bash
curl -X DELETE http://localhost:8080/api/v1/product/1
```

### Get Products by Category
```bash
curl "http://localhost:8080/api/v1/product/search?categoryName=Electronics"
```

### Get Distinct Categories
```bash
curl http://localhost:8080/api/v1/product/categories
```

---

## Notes

- All responses are in JSON format.
- No `ResponseEntity` wrappers are used; Spring handles serialization to HTTP 200 OK.
- `Create Product`, `Update Product`, and `Get Products by Category` return the raw `Product` entity, so responses include the nested `category` object and audit fields (`createdAt`, `updatedAt`, `deletedAt`). `Get All Products` and `Get Product by ID` return the trimmed `GetProductResponseDto` instead.
- Products are soft-deleted (`deleted_at` set), not physically removed, and are automatically excluded from all queries once deleted (`@SQLRestriction("deleted_at IS NULL")`).
- `ratings` is a numeric (`BigDecimal`) field.

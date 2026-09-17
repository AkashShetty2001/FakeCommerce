# Order API - Quick Reference

## Base URL
```
http://localhost:8080/api/v1/orders
```

---

## 1. Create Order
**Request:**
```
POST /api/v1/orders
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**Response:** (HTTP 200)
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

**Error Responses:**
- `400` (IllegalArgumentException): Items list is empty or quantity is invalid
- `500` (RuntimeException): Product not found

---

## 2. Get All Orders
**Request:**
```
GET /api/v1/orders/all
```

**Response:** (HTTP 200)
```json
[
  {
    "id": 1,
    "status": "PENDING",
    "items": [...],
    "totalAmount": 35.00
  },
  {
    "id": 2,
    "status": "PROCESSING",
    "items": [...],
    "totalAmount": 50.00
  }
]
```

---

## 3. Get Order by ID
**Request:**
```
GET /api/v1/orders/{id}
```

**Example:**
```
GET /api/v1/orders/1
```

**Response:** (HTTP 200)
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
    }
  ],
  "totalAmount": 20.00
}
```

**Error Responses:**
- `500` (RuntimeException): Order not found

---

## 4. Update Order Status
**Request:**
```
PUT /api/v1/orders/{id}/status
Content-Type: application/json

{
  "status": "PROCESSING"
}
```

**Example:**
```
PUT /api/v1/orders/1/status
{
  "status": "SHIPPED"
}
```

**Valid Status Values:**
- `PENDING`
- `PROCESSING`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

**Response:** (HTTP 200)
```json
{
  "id": 1,
  "status": "SHIPPED",
  "items": [...],
  "totalAmount": 35.00
}
```

**Error Responses:**
- `500` (RuntimeException): Order not found

---

## 5. Delete Order
**Request:**
```
DELETE /api/v1/orders/{id}
```

**Example:**
```
DELETE /api/v1/orders/1
```

**Response:** (HTTP 200)
```
(empty body)
```

**Note:** Delete is soft-delete. The order is marked as deleted in the database but not physically removed.

**Error Responses:**
- `500` (IllegalArgumentException): Order does not exist

---

## DTO Structures

### CreateOrderRequestDto
```json
{
  "items": [
    {
      "productId": <number>,
      "quantity": <number>
    }
  ]
}
```

### OrderResponseDto
```json
{
  "id": <number>,
  "status": "<string>",
  "items": [
    {
      "productId": <number>,
      "productTitle": "<string>",
      "price": <decimal>,
      "quantity": <number>,
      "subtotal": <decimal>
    }
  ],
  "totalAmount": <decimal>
}
```

### UpdateOrderStatusRequestDto
```json
{
  "status": "<string>"
}
```

---

## Field Validation Rules

### CreateOrderRequestDto
- `items`: Must not be null or empty (at least 1 item required)
- `items[].productId`: Must reference an existing product
- `items[].quantity`: Must be greater than 0

### UpdateOrderStatusRequestDto
- `status`: Must be one of: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

---

## Common HTTP Status Codes

| Code | Description |
|------|---|
| 200 | Success (all operations) |
| 400 | Bad request (validation error) |
| 500 | Server error (product/order not found, other exceptions) |

---

## Example cURL Commands

### Create Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2},{"productId":3,"quantity":1}]}'
```

### Get All Orders
```bash
curl http://localhost:8080/api/v1/orders/all
```

### Get Order by ID
```bash
curl http://localhost:8080/api/v1/orders/1
```

### Update Order Status
```bash
curl -X PUT http://localhost:8080/api/v1/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"PROCESSING"}'
```

### Delete Order
```bash
curl -X DELETE http://localhost:8080/api/v1/orders/1
```

---

## Notes

- All responses are in JSON format.
- No ResponseEntity wrappers are used; Spring handles serialization to HTTP 200 OK.
- Order totals are calculated at creation time and persisted in the database.
- Orders are soft-deleted (marked as deleted, not physically removed).
- Ratings have been changed to numeric (BigDecimal) format throughout the system.

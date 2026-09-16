# ResponseEntity & @RestControllerAdvice Guide for FakeCommerce

## Overview

This guide explains two Spring concepts you can adopt in FakeCommerce to improve REST API responses:

- **ResponseEntity** — Gives fine-grained control over HTTP status codes, headers, and response bodies (instead of relying on Spring's defaults).
- **@RestControllerAdvice** — Centralizes exception handling across all controllers into one global class, avoiding scattered try-catch logic in each endpoint.

---

## 1. ResponseEntity — Explicit Response Control

### Current State

All 18 endpoints across your three controllers (`CategoryController`, `OrderController`, `ProductController`) currently return plain types:
- Creation methods (POST) — return the entity/DTO directly → implicit `200 OK`
- Query methods (GET) — return the entity/DTO directly → implicit `200 OK`
- Delete methods (DELETE) — return `void` → implicit `200 OK` with empty body

### What ResponseEntity Provides

`ResponseEntity<T>` lets you specify:
- **Status code** — `201 Created`, `204 No Content`, `400 Bad Request`, etc.
- **Headers** — e.g., `Location` header pointing to the newly created resource
- **Body** — the actual response payload

### Where to Apply It

**Example 1: POST endpoints (create operations)**

Currently:
```java
@PostMapping
public Category createCategory(CreateCategoryRequestDto dto) {
    return categoryService.createCategory(dto);  // returns 200 OK
}
```

Should become something like:
```java
@PostMapping
public ResponseEntity<Category> createCategory(CreateCategoryRequestDto dto) {
    Category created = categoryService.createCategory(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/v1/categories/" + created.getId())
        .body(created);
}
```

Apply this pattern to:
- `CategoryController.createCategory()`
- `OrderController.createOrder()`
- `ProductController.createProduct()`

**Example 2: DELETE endpoints**

Currently:
```java
@DeleteMapping("/{id}")
public void deleteCategoryById(Long id) {
    categoryService.deleteCategoryById(id);  // returns 200 OK with empty body
}
```

Should become something like:
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteCategoryById(Long id) {
    categoryService.deleteCategoryById(id);
    return ResponseEntity.noContent().build();  // returns 204 No Content
}
```

Apply this pattern to:
- `CategoryController.deleteCategoryById()`
- `OrderController.deleteOrderById()`
- `ProductController.deleteProductById()`

### When ResponseEntity Is Optional

Query (GET) endpoints can keep plain return types if the default `200 OK` is acceptable. ResponseEntity is useful there only if you need custom headers or want to return different status codes for edge cases.

---

## 2. @RestControllerAdvice — Centralized Exception Handling

### Current Problem

Services throw exceptions that currently bubble up unhandled:
- `RuntimeException` (from `Optional.orElseThrow()` when entity not found) → Spring returns generic 500 error
- `IllegalArgumentException` (from validation checks like "quantity must be > 0") → Spring returns generic 500 error

There's no global exception mapper, so clients can't distinguish between "not found" and "bad input."

### Solution: Create a Global Exception Handler

Create a new class (e.g., `src/main/java/com/fakecommerce/exception/GlobalExceptionHandler.java`):

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // Most RuntimeExceptions in your services mean "not found"
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Resource not found: " + ex.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        // IllegalArgumentException = bad input/validation failure
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Invalid request: " + ex.getMessage());
    }
}
```

### What This Does

- Any `RuntimeException` thrown by `CategoryService`, `OrderService`, or `ProductService` now returns `404 Not Found` to the client.
- Any `IllegalArgumentException` now returns `400 Bad Request` to the client.
- No controller needs try-catch; the handler catches and formats exceptions globally.

### Exception Types in FakeCommerce

| Exception Type | Where It's Thrown | Suggested Status |
|---|---|---|
| `RuntimeException` (reason: "not found") | `CategoryService.getCategoryById()`, `OrderService.getOrderById()`, `ProductService.getProductById()`, etc. | `404 Not Found` |
| `IllegalArgumentException` | `OrderService.createOrder()` (null/empty items), `ProductService.deleteProductById()` (existence check), etc. | `400 Bad Request` |

### Important Caveat

Mapping generic `RuntimeException` globally will catch **all** runtime errors, not just "not found" cases. If other code in your controllers or services throws `RuntimeException` for unrelated reasons, it will also get a `404` response. This is a design tradeoff — the "proper" solution (mentioned for future reference only) would be to define custom exception classes like `ResourceNotFoundException`, but that's beyond this guide's scope.

---

## 3. Suggested Next Steps

1. **Choose which endpoints to update first:**
   - Start with POST methods (create operations) in all three controllers — these benefit most from `201 Created` + `Location` header.
   - Then update DELETE methods to return `204 No Content`.
   - Leave GET methods as-is for now (optional later).

2. **Create the exception handler:**
   - Create `src/main/java/com/fakecommerce/exception/GlobalExceptionHandler.java` (you may need to create the `exception` package).
   - Add `@RestControllerAdvice` class with `@ExceptionHandler` methods for `RuntimeException` and `IllegalArgumentException`.
   - Test by calling an endpoint with an invalid ID (e.g., `GET /api/v1/categories/999`) and verify you get `404` instead of `500`.

3. **Update controller return types:**
   - Change method signatures from `public Category createCategory(...)` to `public ResponseEntity<Category> createCategory(...)`.
   - Wrap the result: `ResponseEntity.status(HttpStatus.CREATED).body(result)` or `.noContent().build()` for deletes.

4. **Test end-to-end:**
   - Create a new resource (POST) — should return `201` with `Location` header.
   - Query an existing resource (GET) — should still return `200` with body.
   - Delete a resource (DELETE) — should return `204` with empty body.
   - Query a non-existent resource (GET with invalid ID) — should now return `404` (via exception handler) instead of `500`.

---

## References

- [Spring ResponseEntity Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ResponseEntity.html)
- [Spring @RestControllerAdvice Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RestControllerAdvice.html)
- [HTTP Status Codes (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)

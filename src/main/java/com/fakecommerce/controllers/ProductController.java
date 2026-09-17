package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateProductRequestDto;
import com.fakecommerce.dtos.GetProductResponseDto;
import com.fakecommerce.dtos.GetProductWithDetailsDto;
import com.fakecommerce.schema.Product;
import com.fakecommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ProductController handles all HTTP requests related to products.
// All endpoints return ResponseEntity with appropriate HTTP status codes.
@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final  ProductService productService;

    // Returns 200 OK with list of all products
    @GetMapping("/all")
    public ResponseEntity<List<GetProductResponseDto>> getAllProducts(){
        List<GetProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Returns 200 OK with product details for specified ID
    @GetMapping("/{id}")
    public ResponseEntity<GetProductResponseDto> getProductById(@PathVariable Long id){
        GetProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // Returns 200 OK with detailed product information including related data
    @GetMapping("/{id}/details")
    public ResponseEntity<GetProductWithDetailsDto> getProductWithDetailsById(@PathVariable Long id){
        GetProductWithDetailsDto product = productService.getProductWithDetailsById(id);
        return ResponseEntity.ok(product);
    }

    // Returns 201 CREATED with newly created product
    @PostMapping()
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequestDto requestDto){
        Product product = productService.createProduct(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    // Returns 204 NO CONTENT after successful deletion
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id){
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    // Returns 200 OK with list of products filtered by category name
    @GetMapping("/search")
    public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam("categoryName") String category){
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    // Returns 200 OK with list of distinct category names
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getDistinctCategories(){
        List<String> categories = productService.getDistinctCategories();
        return ResponseEntity.ok(categories);
    }

    // Returns 200 OK with updated product details
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProductById(@PathVariable Long id,@RequestBody CreateProductRequestDto requestDto){
        Product product = productService.updateProductById(id, requestDto);
        return ResponseEntity.ok(product);
    }



}

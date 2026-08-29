package com.fakecommerce.controllers;

import com.fakecommerce.dtos.CreateProductRequestDto;
import com.fakecommerce.dtos.GetProductResponseDto;
import com.fakecommerce.dtos.GetProductWithDetailsDto;
import com.fakecommerce.schema.Product;
import com.fakecommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final  ProductService productService;

    @GetMapping("/all")
    public List<GetProductResponseDto> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public GetProductResponseDto getProductById(@PathVariable Long id){
        return productService.getProductById(id);

    }

    @GetMapping("/{id}/details")
    public GetProductWithDetailsDto getProductWithDetailsById(@PathVariable Long id){
        return productService.getProductWithDetailsById(id);
    }

    @PostMapping()
    public Product createProduct(@RequestBody CreateProductRequestDto requestDto){
        return productService.createProduct(requestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable Long id){
        productService.deleteProductById(id);
    }

    @GetMapping("/search")
    public List<Product> getProductsByCategory(@RequestParam("categoryName") String category){
        return productService.getProductsByCategory(category);
    }

    @GetMapping("/categories")
    public List<String> getDistinctCategories(){
        return productService.getDistinctCategories();
    }

    @PutMapping("/{id}")
    public Product updateProductById(@PathVariable Long id,@RequestBody CreateProductRequestDto requestDto){
        return productService.updateProductById(id, requestDto);
    }



}

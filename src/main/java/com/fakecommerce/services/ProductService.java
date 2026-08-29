package com.fakecommerce.services;

import com.fakecommerce.dtos.CreateProductRequestDto;
import com.fakecommerce.dtos.GetProductResponseDto;
import com.fakecommerce.dtos.GetProductWithDetailsDto;
import com.fakecommerce.repository.CategoryRepository;
import com.fakecommerce.repository.ProductRepository;
import com.fakecommerce.schema.Category;
import com.fakecommerce.schema.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /*
        * Get all products from the database
        * its equivalent to SELECT * from Products
     */
    public List<GetProductResponseDto> getAllProducts(){
        List<Product> productList =  productRepository.findAll();

       /* List<GetProductResponseDto> getProductResponseDtos = new ArrayList<>();

        for(Product product : productList){
            GetProductResponseDto getProductResponseDto = GetProductResponseDto.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .image(product.getImage())
                    .ratings(product.getRatings())
                    .description(product.getDescription())
                    .build();
            getProductResponseDtos.add(getProductResponseDto);
        }
        return getProductResponseDtos;*/

        /*
         using stream api
         */

        return productList.stream().
                map(product -> GetProductResponseDto.builder().
                        id(product.getId()).
                        title(product.getTitle()).
                        price(product.getPrice()).
                        image(product.getImage()).
                        ratings(product.getRatings()).
                        description(product.getDescription()).
                        build()
                    ).collect(Collectors.toList());

    }

    /*
        * Get product by id from the database, if not found throw an exception
        * its equivalent to SELECT *
                        FROM product
                        WHERE id = ?;
     */
    public GetProductResponseDto getProductById(Long id){
        return  productRepository.findById(id).
                map(product -> GetProductResponseDto.builder()
                        .id(product.getId())
                        .title(product.getTitle())
                                .price((product.getPrice()))
                                .ratings(product.getRatings())
                                .description(product.getDescription())
                        .build())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    }

    /*
        * Create a new product in the database
        * its equivalent to INSERT INTO product (title, price, image, category, ratings, description)
                        VALUES (?, ?, ?, ?, ?, ?);
     */
    public Product createProduct(CreateProductRequestDto requestDto){

        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDto.getCategoryId()));

         Product newProduct = Product.builder()
                 .title(requestDto.getTitle())
                 .price(requestDto.getPrice())
                 .image(requestDto.getImage())
                 .description(requestDto.getDescription())
                 .ratings(requestDto.getRatings())
                 .category(category)
                 .build();

         return productRepository.save(newProduct);
    }

    /*
        * Delete a product by id from the database
        * its equivalent to DELETE FROM product WHERE id = ?;
     */
    public void deleteProductById(Long id){
        // 1. Check if the product exists in the database
        if (!productRepository.existsById(id)) {
            // 2. Throw an exception if it's missing
            throw new IllegalArgumentException("Product with ID " + id + " does not exist.");
        }

         productRepository.deleteById(id);
    }

    /*
        * Get products by category from the database
        * its equivalent to SELECT *
                        FROM product
                        WHERE category = ?;
     */
    public List<Product> getProductsByCategory(String category){
        return productRepository.findByCategory(category);
    }

    /*
        * Get all distinct categories from the database
        * its equivalent to SELECT DISTINCT category FROM product;
     */
    public List<String> getDistinctCategories(){
        return productRepository.findDistinctCategories();
    }


    public Product updateProductById(Long id, CreateProductRequestDto requestDto){
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (requestDto.getTitle() != null) {
            existingProduct.setTitle(requestDto.getTitle());
        }
        if (requestDto.getPrice() != null) {
            existingProduct.setPrice(requestDto.getPrice());
        }
        if (requestDto.getImage() != null) {
            existingProduct.setImage(requestDto.getImage());
        }
        if (requestDto.getDescription() != null) {
            existingProduct.setDescription(requestDto.getDescription());
        }
        if (requestDto.getRatings() != null) {
            existingProduct.setRatings(requestDto.getRatings());
        }
        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDto.getCategoryId()));

            existingProduct.setCategory(category);
        }

        return productRepository.save(existingProduct);
    }


    public GetProductWithDetailsDto getProductWithDetailsById(Long id){
       Product product = productRepository.findProductsWithDetailsById(id)
               .stream()
               .findFirst()
               .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

       return GetProductWithDetailsDto.builder()
               .id(product.getId())
               .title(product.getTitle())
               .price(product.getPrice())
               .image(product.getImage())
               .ratings(product.getRatings())
               .description(product.getDescription())
               .category(product.getCategory())
               .build();
    }

}

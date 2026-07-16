package com.fakecommerce.services;

import com.fakecommerce.dtos.CreateProductRequestDto;
import com.fakecommerce.repository.ProductRepository;
import com.fakecommerce.schema.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final  ProductRepository productRepository;

    /*
        * Get all products from the database
        * its equivalent to SELECT * from Products
     */
    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    /*
        * Get product by id from the database, if not found throw an exception
        * its equivalent to SELECT *
                        FROM product
                        WHERE id = ?;
     */
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product createProduct(CreateProductRequestDto requestDto){
         Product newProduct = Product.builder()
                 .title(requestDto.getTitle())
                 .price(requestDto.getPrice())
                 .image(requestDto.getImage())
                 .description(requestDto.getDescription())
                 .ratings(requestDto.getRatings())
                 .category(requestDto.getCategory())
                 .build();

         return productRepository.save(newProduct);
    }


}

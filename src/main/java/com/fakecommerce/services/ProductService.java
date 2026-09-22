package com.fakecommerce.services;

import com.fakecommerce.dtos.CreateProductRequestDto;
import com.fakecommerce.dtos.GetProductResponseDto;
import com.fakecommerce.dtos.GetProductWithDetailsDto;
import com.fakecommerce.exceptions.CategoryNotFoundException;
import com.fakecommerce.exceptions.ResourceNotFoundException;
import com.fakecommerce.mappers.ProductMapper;
import com.fakecommerce.repository.CategoryRepository;
import com.fakecommerce.repository.ProductRepository;
import com.fakecommerce.schema.Category;
import com.fakecommerce.schema.Product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProductService contains all business logic related to products.
 * Entity <-> DTO mapping is delegated to ProductMapper (MapStruct); category
 * resolution stays here since it requires a CategoryRepository lookup.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Get all products from the database.
     * Equivalent to SELECT * FROM products.
     *
     * @return a list of all products mapped to GetProductResponseDto
     */
    public List<GetProductResponseDto> getAllProducts(){
        List<Product> productList = productRepository.findAll();
        return productMapper.toResponseDtoList(productList);
    }

    /**
     * Get a single product by id.
     * Equivalent to SELECT * FROM product WHERE id = ?.
     *
     * @param id the product ID to retrieve
     * @return GetProductResponseDto for the found product
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    public GetProductResponseDto getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toResponseDto(product);
    }

    /**
     * Create a new product.
     * Equivalent to INSERT INTO product (title, price, image, category, ratings, description) VALUES (...).
     * The category is resolved from categoryId before saving since MapStruct
     * cannot look up an entity from just an id.
     *
     * @param requestDto the request DTO with product fields and categoryId
     * @return GetProductResponseDto for the newly created product
     * @throws CategoryNotFoundException if the referenced category does not exist
     */
    public GetProductResponseDto createProduct(CreateProductRequestDto requestDto){
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + requestDto.getCategoryId()));

        Product newProduct = productMapper.toEntity(requestDto);
        newProduct.setCategory(category);

        Product savedProduct = productRepository.save(newProduct);
        return productMapper.toResponseDto(savedProduct);
    }

    /**
     * Delete a product by id.
     * Equivalent to DELETE FROM product WHERE id = ?.
     *
     * @param id the product ID to delete
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    public void deleteProductById(Long id){
        // 1. Check if the product exists in the database
        if (!productRepository.existsById(id)) {
            // 2. Throw an exception if it's missing
            throw new ResourceNotFoundException("Product with ID " + id + " does not exist.");
        }

        productRepository.deleteById(id);
    }

    /**
     * Get products filtered by category name.
     * Equivalent to SELECT * FROM product WHERE category = ?.
     *
     * @param category the category name to filter by
     * @return a list of matching products mapped to GetProductResponseDto
     */
    public List<GetProductResponseDto> getProductsByCategory(String category){
        List<Product> products = productRepository.findByCategoryCategoryName(category);
        return productMapper.toResponseDtoList(products);
    }

    /**
     * Get all distinct category names present on products.
     * Equivalent to SELECT DISTINCT category FROM product.
     *
     * @return a list of distinct category names
     */
    public List<String> getDistinctCategories(){
        return productRepository.findDistinctCategories();
    }

    /**
     * Update an existing product. Only non-null fields on the request DTO are
     * applied (partial update), matching the previous manual-if behavior.
     * If categoryId is provided, the category is re-resolved and re-assigned.
     *
     * @param id the product ID to update
     * @param requestDto the request DTO with fields to update
     * @return GetProductResponseDto for the updated product
     * @throws ResourceNotFoundException if no product exists with the given id
     * @throws CategoryNotFoundException if the referenced category does not exist
     */
    public GetProductResponseDto updateProductById(Long id, CreateProductRequestDto requestDto){
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.updateEntityFromDto(requestDto, existingProduct);

        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
            existingProduct.setCategory(category);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toResponseDto(updatedProduct);
    }

    /**
     * Get a single product with its full details, including the resolved category.
     * Equivalent to SELECT * FROM product JOIN category WHERE product.id = ?.
     *
     * @param id the product ID to retrieve
     * @return GetProductWithDetailsDto with the product and its mapped category
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    public GetProductWithDetailsDto getProductWithDetailsById(Long id){
        Product product = productRepository.findProductsWithDetailsById(id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return productMapper.toDetailsResponseDto(product);
    }

}

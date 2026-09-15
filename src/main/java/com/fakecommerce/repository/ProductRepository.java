package com.fakecommerce.repository;

import com.fakecommerce.schema.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

     List<Product> findByCategoryCategoryName(String categoryName);

     /*
        * Get distinct categories from the database
        * JPQL Query
      */
     @Query("SELECT DISTINCT p.category FROM Product p")
     public List<String> findDistinctCategories();

     /*
          we can also have native Query
          @Query(value = "SELECT DISTINCT category FROM products", nativeQuery = true)
          public List<String> findDistinctCategories();
      */

     @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
     List<Product> findProductsWithDetailsById(Long id);
}
package com.fakecommerce.repository;

import com.fakecommerce.schema.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

     public List<Product> findByCategory(String category);

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




}
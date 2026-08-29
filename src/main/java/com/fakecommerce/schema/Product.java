package com.fakecommerce.schema;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicUpdate
@Table(name = "products")
@EqualsAndHashCode(callSuper=true)
public class Product extends BaseEntity {

   /*
        we can have common properties present inside a baseEntity class and use inheritance .
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    */


    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private BigDecimal price;

    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    /*
    read as many products associated to one category.
        The @ManyToOne annotation is used to define a many-to-one relationship between two entities in a relational database.
        In this case, it indicates that each Product entity is associated with one Category entity, while each Category entity can be associated with multiple Product entities.
        The @JoinColumn annotation is used to specify the foreign key column that will be used to establish the relationship between the two entities.
        In this case, the foreign key column is named "category_id" and is marked as not nullable, meaning that every Product must have an associated Category.
     */
    @JoinColumn(name ="category_id",nullable = false)
    private Category category;

    private String ratings;

    @Column(columnDefinition = "TEXT")
    private String description;

}

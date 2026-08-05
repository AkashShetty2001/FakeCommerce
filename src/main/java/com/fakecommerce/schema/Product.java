package com.fakecommerce.schema;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
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

    private String category;

    private String ratings;

    @Column(columnDefinition = "TEXT")
    private String description;

}

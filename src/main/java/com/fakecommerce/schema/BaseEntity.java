package com.fakecommerce.schema;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
/*
    The @MappedSuperclass annotation is used to indicate that this class is a superclass that provides mapping information for its subclasses.
    It allows the subclasses to inherit the mapping information defined in this class, such as the primary key field and any other common fields.
    it's a way of defining an inheritance in spring.
 */
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}

package com.fakecommerce.schema;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "categories")
@EqualsAndHashCode(callSuper=true)
public class Category extends BaseEntity{

    @Column(name="category_name")
    private String categoryName;
}

package com.fakecommerce.schema;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "orders")
public class Order extends BaseEntity{

    private OrderStatus status;
}

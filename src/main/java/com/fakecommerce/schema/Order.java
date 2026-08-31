package com.fakecommerce.schema;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Order extends BaseEntity{

    private OrderStatus status;
}

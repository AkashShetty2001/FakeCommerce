package com.fakecommerce.schema;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

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

    /**
     * Total amount for the entire order (sum of all line items: price × quantity).
     * Persisted to the database via Flyway migration V2.
     * Mapped to the 'total_amount' column using @Column annotation.
     */
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
}

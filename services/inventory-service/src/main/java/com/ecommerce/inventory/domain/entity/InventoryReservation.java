// services/inventory-service/src/main/java/com/ecommerce/inventory/domain/entity/InventoryReservation.java
package com.ecommerce.inventory.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.ProductId;
import com.ecommerce.inventory.domain.valueobject.ReservationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("inventory_reservations")
public class InventoryReservation extends BaseEntity {
    
    @NotNull
    @Column("reservation_id")
    private String reservationId;
    
    @NotNull
    @Column("order_id")
    private OrderId orderId;
    
    @NotNull
    @Column("product_id")
    private ProductId productId;
    
    @NotNull
    @Min(1)
    @Column("quantity")
    private Integer quantity;
    
    @NotNull
    @Column("status")
    private ReservationStatus status;
    
    @Column("expires_at")
    private Instant expiresAt;
    
    @Column("confirmed_at")
    private Instant confirmedAt;
    
    @Column("released_at")
    private Instant releasedAt;
    
    protected InventoryReservation() {
        super();
    }
    
    public InventoryReservation(String reservationId, OrderId orderId, ProductId productId, 
                               Integer quantity, Instant expiresAt) {
        super();
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.ACTIVE;
        this.expiresAt = expiresAt;
    }
    
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }
    
    public void release() {
        this.status = ReservationStatus.RELEASED;
        this.releasedAt = Instant.now();
    }
    
    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }
    
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }
    
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE && !isExpired();
    }
}
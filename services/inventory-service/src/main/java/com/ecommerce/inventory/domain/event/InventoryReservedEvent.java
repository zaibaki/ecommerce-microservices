// services/inventory-service/src/main/java/com/ecommerce/inventory/domain/event/InventoryReservedEvent.java
package com.ecommerce.inventory.domain.event;

import com.ecommerce.domain.common.event.DomainEvent;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.ProductId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryReservedEvent extends DomainEvent {
    
    private final OrderId orderId;
    private final ProductId productId;
    private final Integer quantity;
    private final String reservationId;
    
    public InventoryReservedEvent(OrderId orderId, ProductId productId, 
                                 Integer quantity, String reservationId) {
        super();
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.reservationId = reservationId;
    }
    
    @Override
    public String getAggregateId() {
        return productId.getValue();
    }
}
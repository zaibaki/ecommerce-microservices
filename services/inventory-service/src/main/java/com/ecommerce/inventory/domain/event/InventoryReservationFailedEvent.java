// services/inventory-service/src/main/java/com/ecommerce/inventory/domain/event/InventoryReservationFailedEvent.java
package com.ecommerce.inventory.domain.event;

import com.ecommerce.domain.common.event.DomainEvent;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.ProductId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryReservationFailedEvent extends DomainEvent {
    
    private final OrderId orderId;
    private final List<ProductId> unavailableProducts;
    private final String reason;
    
    public InventoryReservationFailedEvent(OrderId orderId, List<ProductId> unavailableProducts, String reason) {
        super();
        this.orderId = orderId;
        this.unavailableProducts = unavailableProducts;
        this.reason = reason;
    }
    
    @Override
    public String getAggregateId() {
        return orderId.getValue();
    }
}
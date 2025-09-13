// services/inventory-service/src/main/java/com/ecommerce/inventory/domain/event/LowStockAlertEvent.java
package com.ecommerce.inventory.domain.event;

import com.ecommerce.domain.common.event.DomainEvent;
import com.ecommerce.domain.common.valueobject.ProductId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LowStockAlertEvent extends DomainEvent {
    
    private final ProductId productId;
    private final String productName;
    private final String sku;
    private final Integer currentQuantity;
    private final Integer reorderLevel;
    private final String warehouseLocation;
    
    public LowStockAlertEvent(ProductId productId, String productName, String sku,
                             Integer currentQuantity, Integer reorderLevel, String warehouseLocation) {
        super();
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.currentQuantity = currentQuantity;
        this.reorderLevel = reorderLevel;
        this.warehouseLocation = warehouseLocation;
    }
    
    @Override
    public String getAggregateId() {
        return productId.getValue();
    }
}
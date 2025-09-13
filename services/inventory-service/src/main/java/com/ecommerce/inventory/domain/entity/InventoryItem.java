// services/inventory-service/src/main/java/com/ecommerce/inventory/domain/entity/InventoryItem.java
package com.ecommerce.inventory.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.exception.BusinessRuleViolationException;
import com.ecommerce.domain.common.valueobject.ProductId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("inventory_items")
public class InventoryItem extends BaseEntity {
    
    @NotNull
    @Column("product_id")
    private ProductId productId;
    
    @NotBlank
    @Column("product_name")
    private String productName;
    
    @NotBlank
    @Column("sku")
    private String sku;
    
    @NotNull
    @Min(0)
    @Column("quantity_available")
    private Integer quantityAvailable;
    
    @NotNull
    @Min(0)
    @Column("quantity_reserved")
    private Integer quantityReserved;
    
    @NotNull
    @Min(0)
    @Column("reorder_level")
    private Integer reorderLevel;
    
    @NotNull
    @Min(0)
    @Column("max_stock_level")
    private Integer maxStockLevel;
    
    @Column("unit_cost")
    private BigDecimal unitCost;
    
    @NotBlank
    @Column("warehouse_location")
    private String warehouseLocation;
    
    @Column("supplier_id")
    private String supplierId;
    
    @Column("supplier_name")
    private String supplierName;
    
    @Column("category")
    private String category;
    
    @Column("description")
    private String description;
    
    @Column("is_active")
    private Boolean isActive;
    
    protected InventoryItem() {
        super();
    }
    
    public InventoryItem(ProductId productId, String productName, String sku,
                        Integer initialQuantity, Integer reorderLevel, Integer maxStockLevel,
                        BigDecimal unitCost, String warehouseLocation) {
        super();
        validateInventoryData(initialQuantity, reorderLevel, maxStockLevel);
        
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.quantityAvailable = initialQuantity;
        this.quantityReserved = 0;
        this.reorderLevel = reorderLevel;
        this.maxStockLevel = maxStockLevel;
        this.unitCost = unitCost;
        this.warehouseLocation = warehouseLocation;
        this.isActive = true;
    }
    
    public void reserveQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("Reservation quantity must be positive");
        }
        
        if (quantityAvailable < quantity) {
            throw new BusinessRuleViolationException(
                String.format("Insufficient inventory. Available: %d, Requested: %d", 
                    quantityAvailable, quantity));
        }
        
        this.quantityAvailable -= quantity;
        this.quantityReserved += quantity;
    }
    
    public void releaseReservation(Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("Release quantity must be positive");
        }
        
        if (quantityReserved < quantity) {
            throw new BusinessRuleViolationException(
                String.format("Cannot release more than reserved. Reserved: %d, Requested: %d", 
                    quantityReserved, quantity));
        }
        
        this.quantityReserved -= quantity;
        this.quantityAvailable += quantity;
    }
    
    public void confirmReservation(Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("Confirmation quantity must be positive");
        }
        
        if (quantityReserved < quantity) {
            throw new BusinessRuleViolationException(
                String.format("Cannot confirm more than reserved. Reserved: %d, Requested: %d", 
                    quantityReserved, quantity));
        }
        
        this.quantityReserved -= quantity;
        // Quantity is removed from both available and reserved (sold)
    }
    
    public void addStock(Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("Stock addition quantity must be positive");
        }
        
        Integer newTotal = quantityAvailable + quantityReserved + quantity;
        if (newTotal > maxStockLevel) {
            throw new BusinessRuleViolationException(
                String.format("Adding stock would exceed maximum level. Max: %d, New Total: %d", 
                    maxStockLevel, newTotal));
        }
        
        this.quantityAvailable += quantity;
    }
    
    public void updateUnitCost(BigDecimal newUnitCost) {
        if (newUnitCost == null || newUnitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Unit cost must be non-negative");
        }
        this.unitCost = newUnitCost;
    }
    
    public void updateReorderLevel(Integer newReorderLevel) {
        if (newReorderLevel < 0) {
            throw new BusinessRuleViolationException("Reorder level must be non-negative");
        }
        this.reorderLevel = newReorderLevel;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public boolean isLowStock() {
        return quantityAvailable <= reorderLevel;
    }
    
    public boolean isOutOfStock() {
        return quantityAvailable <= 0;
    }
    
    public Integer getTotalQuantity() {
        return quantityAvailable + quantityReserved;
    }
    
    public boolean canReserve(Integer quantity) {
        return isActive && quantityAvailable >= quantity;
    }
    
    private void validateInventoryData(Integer initialQuantity, Integer reorderLevel, Integer maxStockLevel) {
        if (initialQuantity < 0) {
            throw new BusinessRuleViolationException("Initial quantity cannot be negative");
        }
        
        if (reorderLevel < 0) {
            throw new BusinessRuleViolationException("Reorder level cannot be negative");
        }
        
        if (maxStockLevel <= 0) {
            throw new BusinessRuleViolationException("Maximum stock level must be positive");
        }
        
        if (initialQuantity > maxStockLevel) {
            throw new BusinessRuleViolationException("Initial quantity cannot exceed maximum stock level");
        }
        
        if (reorderLevel >= maxStockLevel) {
            throw new BusinessRuleViolationException("Reorder level must be less than maximum stock level");
        }
    }
}
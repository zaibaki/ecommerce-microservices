// services/inventory-service/src/main/java/com/ecommerce/inventory/domain/repository/InventoryItemRepository.java
package com.ecommerce.inventory.domain.repository;

import com.ecommerce.domain.common.valueobject.ProductId;
import com.ecommerce.inventory.domain.entity.InventoryItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface InventoryItemRepository {
    
    Mono<InventoryItem> save(InventoryItem item);
    
    Mono<InventoryItem> findById(String id);
    
    Mono<InventoryItem> findByProductId(ProductId productId);
    
    Mono<InventoryItem> findBySku(String sku);
    
    Flux<InventoryItem> findByWarehouseLocation(String warehouseLocation);
    
    Flux<InventoryItem> findLowStockItems();
    
    Flux<InventoryItem> findOutOfStockItems();
    
    Flux<InventoryItem> findByCategory(String category);
    
    Flux<InventoryItem> findByProductIds(List<ProductId> productIds);
    
    Flux<InventoryItem> findActiveItems();
    
    Mono<Boolean> existsByProductId(ProductId productId);
    
    Mono<Boolean> existsBySku(String sku);
    
    Mono<Void> deleteById(String id);
    
    Mono<Long> countLowStockItems();
    
    Mono<Long> countOutOfStockItems();
}
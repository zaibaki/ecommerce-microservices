// services/analytics-service/src/main/java/com/ecommerce/analytics/domain/entity/OrderAnalytics.java
package com.ecommerce.analytics.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.enums.OrderStatus;
import com.ecommerce.domain.common.valueobject.Money;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.UserId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("order_analytics")
public class OrderAnalytics extends BaseEntity {
    
    @NotNull
    @Column("order_id")
    private OrderId orderId;
    
    @NotNull
    @Column("customer_id")
    private UserId customerId;
    
    @NotNull
    @Column("order_amount")
    private Money orderAmount;
    
    @NotNull
    @Column("order_status")
    private OrderStatus orderStatus;
    
    @Column("items_count")
    private Integer itemsCount;
    
    @Column("payment_method")
    private String paymentMethod;
    
    @Column("shipping_country")
    private String shippingCountry;
    
    @Column("shipping_city")
    private String shippingCity;
    
    @Column("customer_segment")
    private String customerSegment;
    
    @Column("order_source")
    private String orderSource;
    
    @Column("promotion_code")
    private String promotionCode;
    
    @Column("discount_amount")
    private Money discountAmount;
    
    @Column("processing_time_seconds")
    private Long processingTimeSeconds;
    
    @Column("order_date")
    private Instant orderDate;
    
    @Column("fulfilled_date")
    private Instant fulfilledDate;
    
    protected OrderAnalytics() {
        super();
    }
    
    public OrderAnalytics(OrderId orderId, UserId customerId, Money orderAmount, 
                         OrderStatus orderStatus, Integer itemsCount) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderAmount = orderAmount;
        this.orderStatus = orderStatus;
        this.itemsCount = itemsCount;
        this.orderDate = Instant.now();
    }
    
    public void updateStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
        if (newStatus == OrderStatus.DELIVERED) {
            this.fulfilledDate = Instant.now();
            calculateProcessingTime();
        }
    }
    
    private void calculateProcessingTime() {
        if (orderDate != null && fulfilledDate != null) {
            this.processingTimeSeconds = fulfilledDate.getEpochSecond() - orderDate.getEpochSecond();
        }
    }
}

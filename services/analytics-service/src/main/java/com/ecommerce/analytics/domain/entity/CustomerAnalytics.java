// services/analytics-service/src/main/java/com/ecommerce/analytics/domain/entity/CustomerAnalytics.java
package com.ecommerce.analytics.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.valueobject.Money;
import com.ecommerce.domain.common.valueobject.UserId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("customer_analytics")
public class CustomerAnalytics extends BaseEntity {
    
    @NotNull
    @Column("customer_id")
    private UserId customerId;
    
    @Column("total_orders")
    private Integer totalOrders;
    
    @Column("total_spent")
    private Money totalSpent;
    
    @Column("average_order_value")
    private Money averageOrderValue;
    
    @Column("last_order_date")
    private Instant lastOrderDate;
    
    @Column("first_order_date")
    private Instant firstOrderDate;
    
    @Column("customer_lifetime_days")
    private Long customerLifetimeDays;
    
    @Column("preferred_category")
    private String preferredCategory;
    
    @Column("preferred_payment_method")
    private String preferredPaymentMethod;
    
    @Column("customer_segment")
    private String customerSegment;
    
    @Column("is_high_value")
    private Boolean isHighValue;
    
    @Column("risk_score")
    private Double riskScore;
    
    protected CustomerAnalytics() {
        super();
    }
    
    public CustomerAnalytics(UserId customerId) {
        super();
        this.customerId = customerId;
        this.totalOrders = 0;
        this.totalSpent = Money.zero(java.util.Currency.getInstance("USD"));
        this.averageOrderValue = Money.zero(java.util.Currency.getInstance("USD"));
        this.isHighValue = false;
        this.riskScore = 0.0;
    }
    
    public void addOrder(Money orderAmount, Instant orderDate) {
        this.totalOrders++;
        this.totalSpent = this.totalSpent.add(orderAmount);
        this.averageOrderValue = this.totalSpent.divide(java.math.BigDecimal.valueOf(totalOrders));
        
        if (this.firstOrderDate == null) {
            this.firstOrderDate = orderDate;
        }
        this.lastOrderDate = orderDate;
        
        calculateCustomerLifetime();
        updateCustomerSegment();
    }
    
    private void calculateCustomerLifetime() {
        if (firstOrderDate != null && lastOrderDate != null) {
            this.customerLifetimeDays = java.time.Duration.between(firstOrderDate, lastOrderDate).toDays();
        }
    }
    
    private void updateCustomerSegment() {
        if (totalSpent.getAmount().compareTo(java.math.BigDecimal.valueOf(1000)) > 0) {
            this.customerSegment = "HIGH_VALUE";
            this.isHighValue = true;
        } else if (totalSpent.getAmount().compareTo(java.math.BigDecimal.valueOf(500)) > 0) {
            this.customerSegment = "MEDIUM_VALUE";
        } else {
            this.customerSegment = "LOW_VALUE";
        }
    }
}
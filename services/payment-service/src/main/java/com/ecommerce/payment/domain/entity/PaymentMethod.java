// services/payment-service/src/main/java/com/ecommerce/payment/domain/entity/PaymentMethod.java
package com.ecommerce.payment.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.valueobject.UserId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("payment_methods")
public class PaymentMethod extends BaseEntity {
    
    @NotNull
    @Column("customer_id")
    private UserId customerId;
    
    @NotBlank
    @Column("gateway_payment_method_id")
    private String gatewayPaymentMethodId;
    
    @NotNull
    @Column("type")
    private com.ecommerce.payment.domain.valueobject.PaymentMethod type;
    
    @Column("last_four_digits")
    private String lastFourDigits;
    
    @Column("expiry_month")
    private Integer expiryMonth;
    
    @Column("expiry_year")
    private Integer expiryYear;
    
    @Column("card_brand")
    private String cardBrand;
    
    @Column("is_default")
    private Boolean isDefault;
    
    @Column("is_active")
    private Boolean isActive;
    
    protected PaymentMethod() {
        super();
    }
    
    public PaymentMethod(UserId customerId, String gatewayPaymentMethodId, 
                        com.ecommerce.payment.domain.valueobject.PaymentMethod type,
                        String lastFourDigits, Integer expiryMonth, Integer expiryYear, 
                        String cardBrand) {
        super();
        this.customerId = customerId;
        this.gatewayPaymentMethodId = gatewayPaymentMethodId;
        this.type = type;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardBrand = cardBrand;
        this.isDefault = false;
        this.isActive = true;
    }
    
    public void markAsDefault() {
        this.isDefault = true;
    }
    
    public void unmarkAsDefault() {
        this.isDefault = false;
    }
    
    public void deactivate() {
        this.isActive = false;
        this.isDefault = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate expiry = java.time.LocalDate.of(expiryYear, expiryMonth, 1)
            .plusMonths(1).minusDays(1);
        
        return expiry.isBefore(now);
    }
}
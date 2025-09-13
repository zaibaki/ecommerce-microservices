// services/payment-service/src/main/java/com/ecommerce/payment/domain/event/FraudDetectedEvent.java
package com.ecommerce.payment.domain.event;

import com.ecommerce.domain.common.event.DomainEvent;
import com.ecommerce.domain.common.valueobject.Money;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.UserId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FraudDetectedEvent extends DomainEvent {
    
    private final String paymentIntentId;
    private final OrderId orderId;
    private final UserId customerId;
    private final Money amount;
    private final Double fraudScore;
    private final String fraudReason;
    private final String ipAddress;
    
    public FraudDetectedEvent(String paymentIntentId, OrderId orderId, UserId customerId,
                             Money amount, Double fraudScore, String fraudReason, String ipAddress) {
        super();
        this.paymentIntentId = paymentIntentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.fraudScore = fraudScore;
        this.fraudReason = fraudReason;
        this.ipAddress = ipAddress;
    }
    
    @Override
    public String getAggregateId() {
        return paymentIntentId;
    }
}
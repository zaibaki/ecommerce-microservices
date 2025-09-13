// services/payment-service/src/main/java/com/ecommerce/payment/domain/event/PaymentProcessedEvent.java
package com.ecommerce.payment.domain.event;

import com.ecommerce.domain.common.event.DomainEvent;
import com.ecommerce.domain.common.enums.PaymentStatus;
import com.ecommerce.domain.common.valueobject.Money;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.UserId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentProcessedEvent extends DomainEvent {
    
    private final String paymentIntentId;
    private final OrderId orderId;
    private final UserId customerId;
    private final Money amount;
    private final PaymentStatus status;
    private final String gatewayTransactionId;
    
    public PaymentProcessedEvent(String paymentIntentId, OrderId orderId, UserId customerId,
                               Money amount, PaymentStatus status, String gatewayTransactionId) {
        super();
        this.paymentIntentId = paymentIntentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.gatewayTransactionId = gatewayTransactionId;
    }
    
    @Override
    public String getAggregateId() {
        return paymentIntentId;
    }
}
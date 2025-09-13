// services/payment-service/src/main/java/com/ecommerce/payment/domain/entity/Payment.java
package com.ecommerce.payment.domain.entity;

import com.ecommerce.domain.common.entity.BaseEntity;
import com.ecommerce.domain.common.enums.PaymentStatus;
import com.ecommerce.domain.common.exception.BusinessRuleViolationException;
import com.ecommerce.domain.common.valueobject.Money;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.payment.domain.valueobject.PaymentMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("payments")
public class Payment extends BaseEntity {
    
    @NotNull
    @Column("payment_intent_id")
    private String paymentIntentId;
    
    @NotNull
    @Column("order_id")
    private OrderId orderId;
    
    @NotNull
    @Column("customer_id")
    private UserId customerId;
    
    @NotNull
    @Valid
    @Column("amount")
    private Money amount;
    
    @NotNull
    @Column("status")
    private PaymentStatus status;
    
    @NotNull
    @Column("payment_method")
    private PaymentMethod paymentMethod;
    
    @Column("payment_method_id")
    private String paymentMethodId;
    
    @Column("gateway_transaction_id")
    private String gatewayTransactionId;
    
    @Column("gateway_response")
    private String gatewayResponse;
    
    @Column("failure_reason")
    private String failureReason;
    
    @Column("processed_at")
    private Instant processedAt;
    
    @Column("refunded_amount")
    private Money refundedAmount;
    
    @Column("fraud_score")
    private Double fraudScore;
    
    @Column("fraud_check_result")
    private String fraudCheckResult;
    
    @Column("ip_address")
    private String ipAddress;
    
    @Column("user_agent")
    private String userAgent;
    
    protected Payment() {
        super();
    }
    
    public Payment(String paymentIntentId, OrderId orderId, UserId customerId, 
                  Money amount, PaymentMethod paymentMethod, String paymentMethodId,
                  String ipAddress, String userAgent) {
        super();
        this.paymentIntentId = paymentIntentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentMethodId = paymentMethodId;
        this.status = PaymentStatus.PENDING;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.refundedAmount = Money.zero(amount.getCurrency());
    }
    
    public void markProcessing() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleViolationException(
                "Payment can only be processed from PENDING status. Current status: " + status);
        }
        this.status = PaymentStatus.PROCESSING;
    }
    
    public void markCompleted(String gatewayTransactionId, String gatewayResponse) {
        if (status != PaymentStatus.PROCESSING) {
            throw new BusinessRuleViolationException(
                "Payment can only be completed from PROCESSING status. Current status: " + status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponse = gatewayResponse;
        this.processedAt = Instant.now();
    }
    
    public void markFailed(String failureReason, String gatewayResponse) {
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.REFUNDED) {
            throw new BusinessRuleViolationException(
                "Cannot mark completed or refunded payment as failed. Current status: " + status);
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.gatewayResponse = gatewayResponse;
        this.processedAt = Instant.now();
    }
    
    public void markCancelled() {
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.REFUNDED) {
            throw new BusinessRuleViolationException(
                "Cannot cancel completed or refunded payment. Current status: " + status);
        }
        this.status = PaymentStatus.CANCELLED;
    }
    
    public void processRefund(Money refundAmount) {
        if (status != PaymentStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                "Can only refund completed payments. Current status: " + status);
        }
        
        Money totalRefunded = this.refundedAmount.add(refundAmount);
        if (totalRefunded.isGreaterThan(this.amount)) {
            throw new BusinessRuleViolationException(
                "Refund amount exceeds payment amount. Payment: " + this.amount.getAmount() + 
                ", Total Refunds: " + totalRefunded.getAmount());
        }
        
        this.refundedAmount = totalRefunded;
        
        if (this.refundedAmount.equals(this.amount)) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }
    
    public void setFraudScore(Double fraudScore, String fraudCheckResult) {
        this.fraudScore = fraudScore;
        this.fraudCheckResult = fraudCheckResult;
    }
    
    public boolean isProcessable() {
        return status == PaymentStatus.PENDING;
    }
    
    public boolean isRefundable() {
        return status == PaymentStatus.COMPLETED || status == PaymentStatus.PARTIALLY_REFUNDED;
    }
    
    public Money getAvailableRefundAmount() {
        return amount.subtract(refundedAmount);
    }
    
    public boolean isHighRiskTransaction() {
        return fraudScore != null && fraudScore > 0.7;
    }
}
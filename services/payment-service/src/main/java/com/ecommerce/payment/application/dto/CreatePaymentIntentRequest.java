// services/payment-service/src/main/java/com/ecommerce/payment/application/dto/CreatePaymentIntentRequest.java
package com.ecommerce.payment.application.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreatePaymentIntentRequest {
    
    @NotBlank
    private String orderId;
    
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    
    @NotBlank
    private String currency;
    
    @NotBlank
    private String paymentMethodId;
    
    private String description;
}
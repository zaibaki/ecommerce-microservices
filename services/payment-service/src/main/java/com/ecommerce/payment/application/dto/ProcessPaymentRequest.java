// services/payment-service/src/main/java/com/ecommerce/payment/application/dto/ProcessPaymentRequest.java
package com.ecommerce.payment.application.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ProcessPaymentRequest {
    
    @NotBlank
    private String paymentIntentId;
    
    @NotBlank
    private String paymentMethodId;
    
    private String cvv;
    
    private boolean savePaymentMethod = false;
}
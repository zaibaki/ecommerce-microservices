// services/payment-service/src/main/java/com/ecommerce/payment/application/usecase/ProcessPaymentUseCase.java
package com.ecommerce.payment.application.usecase;

import com.ecommerce.domain.common.exception.BusinessRuleViolationException;
import com.ecommerce.domain.common.exception.ResourceNotFoundException;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.UserId;
import com.ecommerce.payment.application.dto.ProcessPaymentRequest;
import com.ecommerce.payment.application.service.FraudDetectionService;
import com.ecommerce.payment.application.service.PaymentEventPublisher;
import com.ecommerce.payment.application.service.PaymentGatewayService;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.event.FraudDetectedEvent;
import com.ecommerce.payment.domain.event.PaymentProcessedEvent;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentUseCase {
    
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final FraudDetectionService fraudDetectionService;
    private final PaymentEventPublisher eventPublisher;
    
    @Transactional
    public Mono<Payment> execute(ProcessPaymentRequest request) {
        log.info("Processing payment for intent: {}", request.getPaymentIntentId());
        
        return findPayment(request.getPaymentIntentId())
            .flatMap(payment -> performFraudCheck(payment, request))
            .flatMap(payment -> processPaymentWithGateway(payment, request))
            .flatMap(paymentRepository::save)
            .doOnSuccess(payment -> {
                log.info("Payment processed successfully: {}", payment.getPaymentIntentId());
                publishPaymentProcessedEvent(payment);
            })
            .doOnError(error -> log.error("Failed to process payment: {}", 
                request.getPaymentIntentId(), error));
    }
    
    private Mono<Payment> findPayment(String paymentIntentId) {
        return paymentRepository.findByPaymentIntentId(paymentIntentId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "Payment not found: " + paymentIntentId)));
    }
    
    private Mono<Payment> performFraudCheck(Payment payment, ProcessPaymentRequest request) {
        return fraudDetectionService.checkForFraud(payment, request)
            .flatMap(fraudResult -> {
                payment.setFraudScore(fraudResult.getFraudScore(), fraudResult.getResult());
                
                if (fraudResult.isHighRisk()) {
                    publishFraudDetectedEvent(payment, fraudResult.getReason());
                    return Mono.error(new BusinessRuleViolationException(
                        "Payment blocked due to fraud detection: " + fraudResult.getReason()));
                }
                
                return Mono.just(payment);
            });
    }
    
    private Mono<Payment> processPaymentWithGateway(Payment payment, ProcessPaymentRequest request) {
        payment.markProcessing();
        
        return paymentGatewayService.processPayment(payment, request)
            .flatMap(gatewayResult -> {
                if (gatewayResult.isSuccess()) {
                    payment.markCompleted(
                        gatewayResult.getTransactionId(), 
                        gatewayResult.getGatewayResponse()
                    );
                } else {
                    payment.markFailed(
                        gatewayResult.getFailureReason(), 
                        gatewayResult.getGatewayResponse()
                    );
                }
                return Mono.just(payment);
            })
            .onErrorResume(error -> {
                payment.markFailed("Gateway error: " + error.getMessage(), null);
                return Mono.just(payment);
            });
    }
    
    private void publishPaymentProcessedEvent(Payment payment) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
            payment.getPaymentIntentId(),
            payment.getOrderId(),
            payment.getCustomerId(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getGatewayTransactionId()
        );
        eventPublisher.publishPaymentProcessed(event);
    }
    
    private void publishFraudDetectedEvent(Payment payment, String fraudReason) {
        FraudDetectedEvent event = new FraudDetectedEvent(
            payment.getPaymentIntentId(),
            payment.getOrderId(),
            payment.getCustomerId(),
            payment.getAmount(),
            payment.getFraudScore(),
            fraudReason,
            payment.getIpAddress()
        );
        eventPublisher.publishFraudDetected(event);
    }
}
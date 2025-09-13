// services/analytics-service/src/main/java/com/ecommerce/analytics/application/usecase/ProcessOrderAnalyticsUseCase.java
package com.ecommerce.analytics.application.usecase;

import com.ecommerce.analytics.domain.entity.CustomerAnalytics;
import com.ecommerce.analytics.domain.entity.OrderAnalytics;
import com.ecommerce.analytics.domain.repository.CustomerAnalyticsRepository;
import com.ecommerce.analytics.domain.repository.OrderAnalyticsRepository;
import com.ecommerce.domain.common.enums.OrderStatus;
import com.ecommerce.domain.common.valueobject.Money;
import com.ecommerce.domain.common.valueobject.OrderId;
import com.ecommerce.domain.common.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessOrderAnalyticsUseCase {
    
    private final OrderAnalyticsRepository orderAnalyticsRepository;
    private final CustomerAnalyticsRepository customerAnalyticsRepository;
    
    public Mono<Void> processOrderEvent(OrderId orderId, UserId customerId, Money amount, 
                                       OrderStatus status, Integer itemsCount) {
        log.info("Processing order analytics for order: {}", orderId);
        
        return processOrderAnalytics(orderId, customerId, amount, status, itemsCount)
            .then(processCustomerAnalytics(customerId, amount, Instant.now()))
            .doOnSuccess(unused -> log.info("Order analytics processed for order: {}", orderId))
            .doOnError(error -> log.error("Failed to process order analytics: {}", orderId, error));
    }
    
    private Mono<OrderAnalytics> processOrderAnalytics(OrderId orderId, UserId customerId, 
                                                      Money amount, OrderStatus status, Integer itemsCount) {
        return orderAnalyticsRepository.findByOrderId(orderId)
            .switchIfEmpty(Mono.fromSupplier(() -> 
                new OrderAnalytics(orderId, customerId, amount, status, itemsCount)))
            .flatMap(analytics -> {
                analytics.updateStatus(status);
                return orderAnalyticsRepository.save(analytics);
            });
    }
    
    private Mono<CustomerAnalytics> processCustomerAnalytics(UserId customerId, Money amount, Instant orderDate) {
        return customerAnalyticsRepository.findByCustomerId(customerId)
            .switchIfEmpty(Mono.fromSupplier(() -> new CustomerAnalytics(customerId)))
            .flatMap(analytics -> {
                analytics.addOrder(amount, orderDate);
                return customerAnalyticsRepository.save(analytics);
            });
    }
}

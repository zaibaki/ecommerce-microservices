// services/user-service/src/main/java/com/ecommerce/user/infrastructure/messaging/KafkaUserEventPublisher.java
package com.ecommerce.user.infrastructure.messaging;

import com.ecommerce.user.application.service.UserEventPublisher;
import com.ecommerce.user.domain.event.UserLoggedInEvent;
import com.ecommerce.user.domain.event.UserRegisteredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUserEventPublisher implements UserEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String USER_EVENTS_TOPIC = "user-events";
    
    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        publishEvent(event, "UserRegistered")
            .doOnSuccess(result -> log.info("Published UserRegistered event: {}", event.getUserId()))
            .doOnError(error -> log.error("Failed to publish UserRegistered event: {}", event.getUserId(), error))
            .subscribe();
    }
    
    @Override
    public void publishUserLoggedIn(UserLoggedInEvent event) {
        publishEvent(event, "UserLoggedIn")
            .doOnSuccess(result -> log.info("Published UserLoggedIn event: {}", event.getUserId()))
            .doOnError(error -> log.error("Failed to publish UserLoggedIn event: {}", event.getUserId(), error))
            .subscribe();
    }
    
    private Mono<Void> publishEvent(Object event, String eventType) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
            .flatMap(eventJson -> Mono.fromFuture(
                kafkaTemplate.send(USER_EVENTS_TOPIC, event.toString(), eventJson)
            ))
            .then()
            .onErrorMap(error -> new RuntimeException("Failed to publish " + eventType + " event", error));
    }
}
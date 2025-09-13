// services/analytics-service/src/main/java/com/ecommerce/analytics/AnalyticsServiceApplication.java
package com.ecommerce.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableR2dbcAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
public class AnalyticsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}

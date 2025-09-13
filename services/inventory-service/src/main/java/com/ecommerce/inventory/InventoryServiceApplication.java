// services/inventory-service/src/main/java/com/ecommerce/inventory/InventoryServiceApplication.java
package com.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableR2dbcAuditing
@EnableKafka
@EnableAsync
@EnableTransactionManagement
public class InventoryServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
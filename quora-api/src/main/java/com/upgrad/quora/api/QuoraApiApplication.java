package com.upgrad.quora.api;

import com.upgrad.quora.service.ServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * A Configuration class that can declare one or more @Bean methods and trigger auto-configuration and component scanning.
 * This class launches a Spring Application from Java main method.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.upgrad.quora.service.dao")
@Import(ServiceConfiguration.class)
public class QuoraApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuoraApiApplication.class, args);
    }
}


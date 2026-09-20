package com.insurance.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Insurance Policy Management Platform API.
 *
 * <p>Starts the Spring Boot application with scheduled renewal processing enabled.
 */
@SpringBootApplication
@EnableScheduling
public class InsurancePlatformApplication {

    private static final Logger log = LoggerFactory.getLogger(InsurancePlatformApplication.class);

    /**
     * Boots the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(InsurancePlatformApplication.class, args);
        log.info("Insurance Policy Management API started");
    }
}

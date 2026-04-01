package com.toptal.bookshopv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Bookshop V2 application.
 *
 * <p>This is a RESTful Online Bookshop API built with Spring Boot 3.4 and Java 21.
 * Unlike V1 (which uses H2 + JPA), V2 stores all data in-memory using
 * {@link java.util.concurrent.ConcurrentHashMap} — no database required.</p>
 *
 * <h3>Key features:</h3>
 * <ul>
 *   <li>JWT-based stateless authentication</li>
 *   <li>Role-based authorization (USER / ADMIN)</li>
 *   <li>In-memory storage with thread-safe ConcurrentHashMap</li>
 *   <li>Scheduled cart expiry cleanup</li>
 *   <li>Swagger/OpenAPI documentation at {@code /swagger-ui.html}</li>
 * </ul>
 *
 * <h3>Annotations:</h3>
 * <ul>
 *   <li>{@code @SpringBootApplication} — enables auto-configuration, component scanning, and configuration</li>
 *   <li>{@code @EnableScheduling} — activates Spring's scheduled task execution for
 *       {@link com.toptal.bookshopv2.service.CartExpiryScheduler}</li>
 * </ul>
 *
 * @author Nitish
 * @version 2.0
 * @see com.toptal.bookshopv2.config.SecurityConfig
 * @see com.toptal.bookshopv2.service.CartExpiryScheduler
 */
@SpringBootApplication
@EnableScheduling
public class BookshopV2Application {

    /**
     * Application entry point. Bootstraps the Spring application context,
     * initializes all beans, starts the embedded Tomcat server, and begins
     * listening for HTTP requests on the configured port (default: 8080).
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SpringApplication.run(BookshopV2Application.class, args);
    }
}

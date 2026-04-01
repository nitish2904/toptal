package com.toptal.bookshopv3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
/** Entry point for the Bookshop Spring Boot application. Enables scheduling for cart expiry. */

@SpringBootApplication
@EnableScheduling
public class BookshopV3Application {
    public static void main(String[] args) {
        SpringApplication.run(BookshopV3Application.class, args);
    }
}

package com.toptal.bookshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
/** Entry point for the Bookshop Spring Boot application. Enables scheduling for cart expiry. */

@SpringBootApplication
@EnableScheduling
public class BookshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookshopApplication.class, args);
    }
}

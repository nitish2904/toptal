package com.toptal.bookshopv3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookshopV3Application {
    public static void main(String[] args) {
        SpringApplication.run(BookshopV3Application.class, args);
    }
}

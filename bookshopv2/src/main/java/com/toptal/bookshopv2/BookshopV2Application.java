package com.toptal.bookshopv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookshopV2Application {
    public static void main(String[] args) {
        SpringApplication.run(BookshopV2Application.class, args);
    }
}

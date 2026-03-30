package com.toptal.bookshopv2.config;

import com.toptal.bookshopv2.model.Role;
import com.toptal.bookshopv2.model.User;
import com.toptal.bookshopv2.store.DataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class DataInitializer implements CommandLineRunner {
    private final DataStore dataStore;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        if (!dataStore.existsUserByEmail("admin@bookshop.com")) {
            dataStore.saveUser(User.builder().email("admin@bookshop.com")
                    .password(passwordEncoder.encode("admin123")).role(Role.ADMIN).build());
            log.info("Default admin user created: admin@bookshop.com / admin123");
        }
    }
}

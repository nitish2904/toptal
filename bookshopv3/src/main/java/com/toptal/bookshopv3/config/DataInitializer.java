package com.toptal.bookshopv3.config;

import com.toptal.bookshopv3.model.Role;
import com.toptal.bookshopv3.model.User;
import com.toptal.bookshopv3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@bookshop.com")) {
            userRepository.save(User.builder().email("admin@bookshop.com")
                    .password(passwordEncoder.encode("admin123")).role(Role.ADMIN).build());
            log.info("Default admin user created: admin@bookshop.com / admin123");
        }
    }
}

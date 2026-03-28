package com.toptal.bookshop.config;

import com.toptal.bookshop.entity.Role;
import com.toptal.bookshop.entity.User;
import com.toptal.bookshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@bookshop.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@bookshop.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("Default admin user created: admin@bookshop.com / admin123");
        }
    }
}

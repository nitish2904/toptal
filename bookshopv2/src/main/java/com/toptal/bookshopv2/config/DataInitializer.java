package com.toptal.bookshopv2.config;

import com.toptal.bookshopv2.model.Role;
import com.toptal.bookshopv2.model.User;
import com.toptal.bookshopv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstrap component that seeds the application with a default admin user on startup.
 *
 * <p>Implements {@link CommandLineRunner} so it executes automatically after the
 * Spring context is fully initialized. Creates an admin user only if one doesn't
 * already exist (idempotent — safe to run on every restart).</p>
 *
 * <h3>Default admin credentials:</h3>
 * <ul>
 *   <li>Email: {@code admin@bookshop.com}</li>
 *   <li>Password: {@code admin123} (BCrypt-hashed before storage)</li>
 *   <li>Role: {@link com.toptal.bookshopv2.model.Role#ADMIN}</li>
 * </ul>
 *
 * @author Nitish
 * @version 2.0
 * @see UserRepository
 */
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

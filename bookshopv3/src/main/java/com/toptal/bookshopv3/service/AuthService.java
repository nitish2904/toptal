package com.toptal.bookshopv3.service;

import com.toptal.bookshopv3.dto.AuthRequest;
import com.toptal.bookshopv3.dto.AuthResponse;
import com.toptal.bookshopv3.exception.ConflictException;
import com.toptal.bookshopv3.model.Role;
import com.toptal.bookshopv3.model.User;
import com.toptal.bookshopv3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
/** Service handling user registration and login, returning a JWT token on success. */

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered: " + request.getEmail());
        User user = User.builder().email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())).role(Role.USER).build();
        user = userRepository.save(user);
        return AuthResponse.builder().email(user.getEmail()).role(user.getRole().name()).build();
    }
}

package com.toptal.bookshopv2.service;

import com.toptal.bookshopv2.dto.AuthRequest;
import com.toptal.bookshopv2.dto.AuthResponse;
import com.toptal.bookshopv2.exception.ConflictException;
import com.toptal.bookshopv2.model.Role;
import com.toptal.bookshopv2.model.User;
import com.toptal.bookshopv2.repository.UserRepository;
import com.toptal.bookshopv2.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered: " + request.getEmail());
        User user = User.builder().email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())).role(Role.USER).build();
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return AuthResponse.builder().token(token).email(user.getEmail()).role(user.getRole().name()).build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return AuthResponse.builder().token(token).email(user.getEmail()).role(user.getRole().name()).build();
    }
}

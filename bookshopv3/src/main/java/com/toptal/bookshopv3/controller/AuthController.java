package com.toptal.bookshopv3.controller;

import com.toptal.bookshopv3.dto.AuthRequest;
import com.toptal.bookshopv3.dto.AuthResponse;
import com.toptal.bookshopv3.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
/** REST controller exposing user registration and login endpoints. */

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        return authService.register(request);
    }
}

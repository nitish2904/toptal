package com.toptal.bookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
/** Response DTO returned after successful authentication, containing the JWT token, email, and role. */

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
}

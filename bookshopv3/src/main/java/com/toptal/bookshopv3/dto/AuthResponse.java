package com.toptal.bookshopv3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/** Response DTO returned after successful authentication, containing the JWT token, email, and role. */

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String email;
    private String role;
}

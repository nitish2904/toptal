package com.toptal.bookshopv2.dto;
import lombok.*;
/** Response DTO returned after successful authentication (JWT token, email, role). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse { private String token; private String email; private String role; }

package com.toptal.bookshopv2.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse { private String token; private String email; private String role; }

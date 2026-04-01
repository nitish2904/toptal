package com.toptal.bookshopv3.model;
import lombok.*;
/** JPA entity representing a registered user with email, password, and role. */
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
    @Builder.Default private Role role = Role.USER;
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}

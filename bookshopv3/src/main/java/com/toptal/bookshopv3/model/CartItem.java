package com.toptal.bookshopv3.model;
import lombok.*;
/** JPA entity representing a single item in a user's shopping cart. */
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItem {
    private Long id;
    private Long userId;
    private Long bookId;
    @Builder.Default private LocalDateTime addedAt = LocalDateTime.now();
}

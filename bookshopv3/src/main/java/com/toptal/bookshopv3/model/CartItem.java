package com.toptal.bookshopv3.model;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItem {
    private Long id;
    private Long userId;
    private Long bookId;
    @Builder.Default private LocalDateTime addedAt = LocalDateTime.now();
}

package com.toptal.bookshopv2.dto;
import com.toptal.bookshopv2.model.Book;
import com.toptal.bookshopv2.model.CartItem;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Response DTO for a cart item with denormalized book details and timestamp. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItemResponse {
    private Long id; private Long bookId; private String bookTitle; private String bookAuthor;
    private BigDecimal bookPrice; private LocalDateTime addedAt;
    public static CartItemResponse from(CartItem ci, Book b) {
        return CartItemResponse.builder().id(ci.getId()).bookId(b.getId()).bookTitle(b.getTitle())
                .bookAuthor(b.getAuthor()).bookPrice(b.getPrice()).addedAt(ci.getAddedAt()).build();
    }
}

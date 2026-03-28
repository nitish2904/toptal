package com.toptal.bookshop.dto;

import com.toptal.bookshop.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private BigDecimal bookPrice;
    private LocalDateTime addedAt;

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getAuthor(),
                item.getBook().getPrice(),
                item.getAddedAt()
        );
    }
}

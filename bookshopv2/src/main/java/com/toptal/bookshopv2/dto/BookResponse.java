package com.toptal.bookshopv2.dto;
import com.toptal.bookshopv2.model.Book;
import com.toptal.bookshopv2.model.Category;
import lombok.*;
import java.math.BigDecimal;
/** Response DTO representing a book with its category. Includes a factory method from entity. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookResponse {
    private Long id; private String title; private String author; private Integer yearPublished;
    private BigDecimal price; private Integer stock; private CategoryResponse category;
    public static BookResponse from(Book b, Category c) {
        return BookResponse.builder().id(b.getId()).title(b.getTitle()).author(b.getAuthor())
                .yearPublished(b.getYearPublished()).price(b.getPrice()).stock(b.getStock())
                .category(c != null ? CategoryResponse.from(c) : null).build();
    }
}

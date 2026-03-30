package com.toptal.bookshopv3.dto;
import com.toptal.bookshopv3.model.Book;
import com.toptal.bookshopv3.model.Category;
import lombok.*;
import java.math.BigDecimal;
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

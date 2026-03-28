package com.toptal.bookshop.dto;

import com.toptal.bookshop.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private Integer yearPublished;
    private BigDecimal price;
    private Integer stock;
    private CategoryResponse category;

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getYearPublished(),
                book.getPrice(),
                book.getStock(),
                CategoryResponse.from(book.getCategory())
        );
    }
}

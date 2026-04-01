package com.toptal.bookshopv3.model;
import lombok.*;
/** JPA entity representing a book in the catalogue with stock management. */
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private String author;
    private Integer yearPublished;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
}

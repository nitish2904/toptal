package com.toptal.bookshopv2.model;
import lombok.*;
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

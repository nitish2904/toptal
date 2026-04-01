package com.toptal.bookshopv3.model;
/** JPA entity representing a book category. */
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Category {
    private Long id;
    private String name;
}

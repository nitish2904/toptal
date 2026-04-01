package com.toptal.bookshopv3.dto;
import com.toptal.bookshopv3.model.Category;
/** Response DTO representing a category (id + name). */
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryResponse {
    private Long id; private String name;
    public static CategoryResponse from(Category c) {
        return CategoryResponse.builder().id(c.getId()).name(c.getName()).build();
    }
}

package com.toptal.bookshopv2.dto;
import com.toptal.bookshopv2.model.Category;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryResponse {
    private Long id; private String name;
    public static CategoryResponse from(Category c) {
        return CategoryResponse.builder().id(c.getId()).name(c.getName()).build();
    }
}

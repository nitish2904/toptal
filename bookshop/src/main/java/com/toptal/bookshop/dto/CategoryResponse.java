package com.toptal.bookshop.dto;

import com.toptal.bookshop.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
/** Response DTO representing a category (id + name). */

@Data
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}

package com.toptal.bookshopv3.dto;
import jakarta.validation.constraints.*;
/** Request DTO for creating or updating a category. */
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name is required") @Size(max = 100) private String name;
}

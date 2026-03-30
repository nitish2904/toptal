package com.toptal.bookshopv2.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name is required") @Size(max = 100) private String name;
}

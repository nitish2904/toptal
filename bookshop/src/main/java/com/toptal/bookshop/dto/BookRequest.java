package com.toptal.bookshop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author name must not exceed 255 characters")
    private String author;

    @NotNull(message = "Year published is required")
    @Min(value = 1000, message = "Year must be at least 1000")
    @Max(value = 2100, message = "Year must not exceed 2100")
    private Integer yearPublished;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;
}

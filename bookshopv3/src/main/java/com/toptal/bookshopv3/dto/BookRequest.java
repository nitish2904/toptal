package com.toptal.bookshopv3.dto;
import jakarta.validation.constraints.*;
import lombok.*;
/** Request DTO for creating a new book including initial stock. */
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor
public class BookRequest {
    @NotBlank(message = "Title is required") private String title;
    @NotBlank(message = "Author is required") private String author;
    @NotNull(message = "Year published is required") private Integer yearPublished;
    @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be > 0") private BigDecimal price;
    @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative") private Integer stock;
    @NotNull(message = "Category ID is required") private Long categoryId;
}

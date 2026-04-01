package com.toptal.bookshopv2.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
/** Request DTO for updating book metadata (admin). Does not include stock. */
@Data @NoArgsConstructor @AllArgsConstructor
public class BookUpdateRequest {
    @NotBlank(message = "Title is required") private String title;
    @NotBlank(message = "Author is required") private String author;
    @NotNull(message = "Year published is required") private Integer yearPublished;
    @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be > 0") private BigDecimal price;
    @NotNull(message = "Category ID is required") private Long categoryId;
}

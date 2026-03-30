package com.toptal.bookshopv2.model;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default private List<OrderItem> items = new ArrayList<>();
}

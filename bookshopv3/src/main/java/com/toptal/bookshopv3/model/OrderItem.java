package com.toptal.bookshopv3.model;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long bookId;
    private BigDecimal priceAtPurchase;
}

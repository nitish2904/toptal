package com.toptal.bookshopv2.dto;
import com.toptal.bookshopv2.model.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private Long id; private BigDecimal totalPrice; private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemResponse {
        private Long bookId; private String bookTitle; private String bookAuthor; private BigDecimal priceAtPurchase;
    }
    public static OrderResponse from(Order order, Map<Long, Book> bookMap) {
        List<OrderItemResponse> ir = order.getItems().stream().map(item -> {
            Book b = bookMap.get(item.getBookId());
            return OrderItemResponse.builder().bookId(item.getBookId())
                    .bookTitle(b != null ? b.getTitle() : "Unknown").bookAuthor(b != null ? b.getAuthor() : "Unknown")
                    .priceAtPurchase(item.getPriceAtPurchase()).build();
        }).toList();
        return OrderResponse.builder().id(order.getId()).totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt()).items(ir).build();
    }
}

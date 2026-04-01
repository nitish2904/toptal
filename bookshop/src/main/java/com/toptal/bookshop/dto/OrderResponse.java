package com.toptal.bookshop.dto;

import com.toptal.bookshop.entity.Order;
import com.toptal.bookshop.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
/** Response DTO for an order with its line items, total price, and creation timestamp. */

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    @Data
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long bookId;
        private String bookTitle;
        private String bookAuthor;
        private BigDecimal priceAtPurchase;

        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getBook().getId(),
                    item.getBook().getTitle(),
                    item.getBook().getAuthor(),
                    item.getPriceAtPurchase()
            );
        }
    }

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}

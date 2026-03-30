package com.toptal.bookshopv2.repository;

import com.toptal.bookshopv2.model.Order;
import com.toptal.bookshopv2.model.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {
    private final AtomicLong orderIdGenerator = new AtomicLong(0);
    private final AtomicLong orderItemIdGenerator = new AtomicLong(0);
    private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();

    public Order save(Order order) {
        if (order.getId() == null) order.setId(orderIdGenerator.incrementAndGet());
        for (OrderItem item : order.getItems()) {
            if (item.getId() == null) item.setId(orderItemIdGenerator.incrementAndGet());
            item.setOrderId(order.getId());
        }
        orders.put(order.getId(), order);
        return order;
    }

    public List<Order> findByUserId(Long userId) {
        return orders.values().stream()
                .filter(o -> o.getUserId().equals(userId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}

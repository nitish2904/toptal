package com.toptal.bookshopv2.repository;

import com.toptal.bookshopv2.model.Order;
import com.toptal.bookshopv2.model.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory repository for {@link Order} and {@link OrderItem} entities.
 *
 * <p>Manages completed purchase orders. Each order contains a list of
 * {@link OrderItem}s that are assigned IDs from a separate sequence.
 * Orders are immutable once created — they cannot be updated or deleted.</p>
 *
 * <h3>ID generation:</h3>
 * <p>Uses two separate {@link AtomicLong} sequences:</p>
 * <ul>
 *   <li>{@code orderIdGenerator} — for {@link Order} IDs</li>
 *   <li>{@code orderItemIdGenerator} — for {@link OrderItem} IDs</li>
 * </ul>
 *
 * <h3>Ordering:</h3>
 * <p>{@link #findByUserId} returns orders sorted by {@code createdAt} descending
 * (newest first), matching typical e-commerce order history behavior.</p>
 *
 * @author Nitish
 * @version 2.0
 * @see Order
 * @see OrderItem
 * @see com.toptal.bookshopv2.service.CartService#checkout
 */
@Repository
public class OrderRepository {

    /** Auto-incrementing ID generator for orders. */
    private final AtomicLong orderIdGenerator = new AtomicLong(0);

    /** Auto-incrementing ID generator for order line items. */
    private final AtomicLong orderItemIdGenerator = new AtomicLong(0);

    /** Primary storage: maps order ID → Order object (including nested OrderItems). */
    private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();

    /**
     * Persists an order and its items to the in-memory store.
     *
     * <p>Assigns auto-incremented IDs to both the order and each of its items.
     * Also sets the {@code orderId} back-reference on each {@link OrderItem}.</p>
     *
     * @param order the order to save, including its {@link Order#getItems()} list;
     *              must not be {@code null}
     * @return the saved order with all IDs populated
     */
    public Order save(Order order) {
        if (order.getId() == null) order.setId(orderIdGenerator.incrementAndGet());
        for (OrderItem item : order.getItems()) {
            if (item.getId() == null) item.setId(orderItemIdGenerator.incrementAndGet());
            item.setOrderId(order.getId());
        }
        orders.put(order.getId(), order);
        return order;
    }

    /**
     * Returns all orders placed by the specified user, sorted by creation date descending.
     *
     * @param userId the ID of the user whose orders to retrieve
     * @return a list of the user's orders (newest first); may be empty but never {@code null}
     */
    public List<Order> findByUserId(Long userId) {
        return orders.values().stream()
                .filter(o -> o.getUserId().equals(userId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}

package com.toptal.bookshopv3.repository;

import com.toptal.bookshopv3.model.CartItem;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
/** Spring Data JPA repository for {@link com.toptal.bookshopv3.entity.CartItem} with bulk-expiry support. */

@Repository
public class CartItemRepository {
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final ConcurrentHashMap<Long, CartItem> cartItems = new ConcurrentHashMap<>();

    public CartItem save(CartItem cartItem) {
        if (cartItem.getId() == null) cartItem.setId(idGenerator.incrementAndGet());
        cartItems.put(cartItem.getId(), cartItem);
        return cartItem;
    }

    public List<CartItem> findByUserId(Long userId) {
        return cartItems.values().stream()
                .filter(ci -> ci.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public boolean existsByUserIdAndBookId(Long userId, Long bookId) {
        return cartItems.values().stream()
                .anyMatch(ci -> ci.getUserId().equals(userId) && ci.getBookId().equals(bookId));
    }

    public Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId) {
        return cartItems.values().stream()
                .filter(ci -> ci.getUserId().equals(userId) && ci.getBookId().equals(bookId))
                .findFirst();
    }

    public void deleteById(Long id) {
        cartItems.remove(id);
    }

    public void deleteByUserId(Long userId) {
        cartItems.entrySet().removeIf(e -> e.getValue().getUserId().equals(userId));
    }

    public void deleteExpiredItems(long expiryMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expiryMinutes);
        cartItems.entrySet().removeIf(e -> e.getValue().getAddedAt().isBefore(cutoff));
    }
}

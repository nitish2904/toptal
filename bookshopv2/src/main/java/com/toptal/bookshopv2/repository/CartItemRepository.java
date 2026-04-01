package com.toptal.bookshopv2.repository;

import com.toptal.bookshopv2.model.CartItem;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory repository for {@link CartItem} entities.
 *
 * <p>Manages the shopping cart state for all users. Uses a {@link ConcurrentHashMap}
 * for thread-safe storage. Provides methods for adding, removing, and querying cart items,
 * as well as bulk operations for checkout cleanup and expiry.</p>
 *
 * <h3>Key operations:</h3>
 * <ul>
 *   <li>{@link #save} — add an item to the cart</li>
 *   <li>{@link #findByUserId} — get all items in a user's cart</li>
 *   <li>{@link #deleteByUserId} — clear entire cart (after checkout)</li>
 *   <li>{@link #deleteExpiredItems} — remove stale items (called by scheduler)</li>
 * </ul>
 *
 * <h3>Used by:</h3>
 * <ul>
 *   <li>{@link com.toptal.bookshopv2.service.CartService} — cart management and checkout</li>
 *   <li>{@link com.toptal.bookshopv2.service.CartExpiryScheduler} — periodic cleanup of expired items</li>
 * </ul>
 *
 * @author Nitish
 * @version 2.0
 * @see CartItem
 * @see com.toptal.bookshopv2.service.CartService
 * @see com.toptal.bookshopv2.service.CartExpiryScheduler
 */
@Repository
public class CartItemRepository {

    /** Auto-incrementing ID generator for new cart items. */
    private final AtomicLong idGenerator = new AtomicLong(0);

    /** Primary storage: maps cart item ID → CartItem object. */
    private final ConcurrentHashMap<Long, CartItem> cartItems = new ConcurrentHashMap<>();

    /**
     * Persists a cart item to the in-memory store.
     *
     * <p>Assigns an auto-incremented ID for new items (where {@code id == null}).</p>
     *
     * @param cartItem the cart item to save; must not be {@code null}
     * @return the saved cart item with its ID populated
     */
    public CartItem save(CartItem cartItem) {
        if (cartItem.getId() == null) cartItem.setId(idGenerator.incrementAndGet());
        cartItems.put(cartItem.getId(), cartItem);
        return cartItem;
    }

    /**
     * Returns all cart items belonging to the specified user.
     *
     * @param userId the ID of the user whose cart to retrieve
     * @return a list of the user's cart items; may be empty but never {@code null}
     */
    public List<CartItem> findByUserId(Long userId) {
        return cartItems.values().stream()
                .filter(ci -> ci.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Checks whether a specific book is already in the user's cart.
     *
     * <p>Used to enforce the "one copy per book per user" rule.</p>
     *
     * @param userId the user's ID
     * @param bookId the book's ID
     * @return {@code true} if this book is already in the user's cart
     */
    public boolean existsByUserIdAndBookId(Long userId, Long bookId) {
        return cartItems.values().stream()
                .anyMatch(ci -> ci.getUserId().equals(userId) && ci.getBookId().equals(bookId));
    }

    /**
     * Finds a specific cart item by user ID and book ID.
     *
     * <p>Used when removing a specific book from the cart.</p>
     *
     * @param userId the user's ID
     * @param bookId the book's ID
     * @return an {@link Optional} containing the cart item if found, or empty if not
     */
    public Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId) {
        return cartItems.values().stream()
                .filter(ci -> ci.getUserId().equals(userId) && ci.getBookId().equals(bookId))
                .findFirst();
    }

    /**
     * Deletes a single cart item by its ID.
     *
     * @param id the ID of the cart item to delete
     */
    public void deleteById(Long id) {
        cartItems.remove(id);
    }

    /**
     * Deletes all cart items belonging to the specified user.
     *
     * <p>Called after successful checkout to clear the user's cart.</p>
     *
     * @param userId the ID of the user whose cart to clear
     */
    public void deleteByUserId(Long userId) {
        cartItems.entrySet().removeIf(e -> e.getValue().getUserId().equals(userId));
    }

    /**
     * Removes all cart items older than the specified number of minutes.
     *
     * <p>Called periodically by {@link com.toptal.bookshopv2.service.CartExpiryScheduler}
     * to prevent stale items from lingering indefinitely. Compares each item's
     * {@link CartItem#getAddedAt()} against the computed cutoff time.</p>
     *
     * @param expiryMinutes the maximum age (in minutes) before an item is considered expired
     */
    public void deleteExpiredItems(long expiryMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expiryMinutes);
        cartItems.entrySet().removeIf(e -> e.getValue().getAddedAt().isBefore(cutoff));
    }
}

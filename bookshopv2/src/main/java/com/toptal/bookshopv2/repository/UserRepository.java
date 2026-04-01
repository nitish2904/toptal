package com.toptal.bookshopv2.repository;

import com.toptal.bookshopv2.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for {@link User} entities.
 *
 * <p>Uses a {@link ConcurrentHashMap} for thread-safe storage and an {@link AtomicLong}
 * for auto-incrementing ID generation. This replaces a traditional JPA/database repository
 * with a lightweight, zero-dependency alternative.</p>
 *
 * <h3>Thread safety:</h3>
 * <p>{@code ConcurrentHashMap} guarantees atomic reads/writes for individual entries.
 * The stream-based lookup methods ({@link #findByEmail}, {@link #existsByEmail}) are
 * safe for concurrent access but not atomic across the full iteration — acceptable
 * since user registration is idempotent (checked via email uniqueness).</p>
 *
 * <h3>Used by:</h3>
 * <ul>
 *   <li>{@link com.toptal.bookshopv2.service.AuthService} — register and login</li>
 *   <li>{@link com.toptal.bookshopv2.security.CustomUserDetailsService} — JWT authentication</li>
 *   <li>{@link com.toptal.bookshopv2.service.CartService} — resolve user from email for cart/order operations</li>
 *   <li>{@link com.toptal.bookshopv2.config.DataInitializer} — seed default admin user</li>
 * </ul>
 *
 * @author Nitish
 * @version 2.0
 * @see User
 * @see com.toptal.bookshopv2.service.AuthService
 */
@Repository
public class UserRepository {

    /** Auto-incrementing ID generator. Starts at 0; first user gets ID 1. */
    private final AtomicLong idGenerator = new AtomicLong(0);

    /** Primary storage: maps user ID → User object. */
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();

    /**
     * Persists a user to the in-memory store.
     *
     * <p>If the user has no ID (new user), an auto-incremented ID is assigned.
     * If the user already has an ID (update), the existing entry is overwritten.</p>
     *
     * @param user the user to save; must not be {@code null}
     * @return the saved user with its ID populated
     */
    public User save(User user) {
        if (user.getId() == null) user.setId(idGenerator.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    /**
     * Finds a user by their email address (case-insensitive).
     *
     * <p>Scans all stored users via stream. Used during login and JWT token validation
     * to resolve the authenticated principal.</p>
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found, or empty if not
     */
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Checks whether a user with the given email address already exists (case-insensitive).
     *
     * <p>Used during registration to prevent duplicate accounts.</p>
     *
     * @param email the email address to check
     * @return {@code true} if a user with this email exists, {@code false} otherwise
     */
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
}

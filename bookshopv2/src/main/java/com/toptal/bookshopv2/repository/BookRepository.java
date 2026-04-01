package com.toptal.bookshopv2.repository;

import com.toptal.bookshopv2.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory repository for {@link Book} entities.
 *
 * <p>Uses a {@link ConcurrentHashMap} for thread-safe storage. Provides methods for
 * CRUD operations plus filtering by stock status and category.</p>
 *
 * <h3>Stock filtering:</h3>
 * <p>The public catalog methods ({@link #findInStock}, {@link #findInStockByCategoryIds})
 * only return books where {@code stock > 0}. Out-of-stock books are hidden from
 * browsing but still accessible by ID (e.g., for cart items added before stock ran out).</p>
 *
 * <h3>Used by:</h3>
 * <ul>
 *   <li>{@link com.toptal.bookshopv2.service.BookService} — catalog browsing, admin CRUD</li>
 *   <li>{@link com.toptal.bookshopv2.service.CartService} — add-to-cart validation, checkout stock decrement</li>
 *   <li>{@link com.toptal.bookshopv2.service.CategoryService} — check if books reference a category before deletion</li>
 * </ul>
 *
 * @author Nitish
 * @version 2.0
 * @see Book
 * @see com.toptal.bookshopv2.service.BookService
 */
@Repository
public class BookRepository {

    /** Auto-incrementing ID generator for new books. */
    private final AtomicLong idGenerator = new AtomicLong(0);

    /** Primary storage: maps book ID → Book object. */
    private final ConcurrentHashMap<Long, Book> books = new ConcurrentHashMap<>();

    /**
     * Persists a book to the in-memory store.
     *
     * <p>Assigns an auto-incremented ID for new books (where {@code id == null}).
     * Also used to update existing books (e.g., stock decrement during checkout).</p>
     *
     * @param book the book to save; must not be {@code null}
     * @return the saved book with its ID populated
     */
    public Book save(Book book) {
        if (book.getId() == null) book.setId(idGenerator.incrementAndGet());
        books.put(book.getId(), book);
        return book;
    }

    /**
     * Finds a book by its unique ID.
     *
     * <p>Returns the book regardless of stock status. Used by cart operations
     * and admin endpoints.</p>
     *
     * @param id the book ID to look up
     * @return an {@link Optional} containing the book if found, or empty if not
     */
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    /**
     * Returns all books that are currently in stock ({@code stock > 0}).
     *
     * <p>Used by the public catalog endpoint to show only available books.</p>
     *
     * @return a list of in-stock books; may be empty but never {@code null}
     */
    public List<Book> findInStock() {
        return books.values().stream()
                .filter(b -> b.getStock() != null && b.getStock() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Returns all in-stock books that belong to any of the specified category IDs.
     *
     * <p>Used for category-filtered catalog browsing.</p>
     *
     * @param categoryIds the list of category IDs to filter by
     * @return a list of matching in-stock books
     */
    public List<Book> findInStockByCategoryIds(List<Long> categoryIds) {
        return books.values().stream()
                .filter(b -> b.getStock() != null && b.getStock() > 0)
                .filter(b -> categoryIds.contains(b.getCategoryId()))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a book by its ID.
     *
     * @param id the ID of the book to delete
     */
    public void deleteById(Long id) {
        books.remove(id);
    }

    /**
     * Checks whether any book references the given category ID.
     *
     * <p>Used by {@link com.toptal.bookshopv2.service.CategoryService#deleteCategory}
     * to prevent deleting categories that still have associated books.</p>
     *
     * @param categoryId the category ID to check
     * @return {@code true} if at least one book belongs to this category
     */
    public boolean existsByCategoryId(Long categoryId) {
        return books.values().stream()
                .anyMatch(b -> categoryId.equals(b.getCategoryId()));
    }

    /**
     * Returns an unmodifiable view of the entire book map.
     *
     * <p>Used by {@link com.toptal.bookshopv2.service.CartService} to efficiently
     * resolve book details when building order responses, avoiding repeated lookups.</p>
     *
     * @return an unmodifiable map of book ID → Book
     */
    public Map<Long, Book> getBookMap() {
        return Collections.unmodifiableMap(books);
    }
}

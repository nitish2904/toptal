package com.toptal.bookshopv2.repository;

import com.toptal.bookshopv2.model.Category;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for {@link Category} entities.
 *
 * <p>Uses a {@link ConcurrentHashMap} for thread-safe storage. Categories represent
 * book genres/topics and are referenced by {@link com.toptal.bookshopv2.model.Book#getCategoryId()}.</p>
 *
 * <h3>Uniqueness:</h3>
 * <p>Category names are unique (case-insensitive). The {@link #existsByName} and
 * {@link #existsByNameAndNotId} methods enforce this constraint at the service layer.</p>
 *
 * <h3>Deletion constraint:</h3>
 * <p>Categories cannot be deleted if any books reference them. This is enforced by
 * {@link com.toptal.bookshopv2.service.CategoryService#deleteCategory} which checks
 * {@link BookRepository#existsByCategoryId} before deletion.</p>
 *
 * @author Nitish
 * @version 2.0
 * @see Category
 * @see com.toptal.bookshopv2.service.CategoryService
 */
@Repository
public class CategoryRepository {

    /** Auto-incrementing ID generator for new categories. */
    private final AtomicLong idGenerator = new AtomicLong(0);

    /** Primary storage: maps category ID → Category object. */
    private final ConcurrentHashMap<Long, Category> categories = new ConcurrentHashMap<>();

    /**
     * Persists a category to the in-memory store.
     *
     * <p>Assigns an auto-incremented ID for new categories (where {@code id == null}).</p>
     *
     * @param category the category to save; must not be {@code null}
     * @return the saved category with its ID populated
     */
    public Category save(Category category) {
        if (category.getId() == null) category.setId(idGenerator.incrementAndGet());
        categories.put(category.getId(), category);
        return category;
    }

    /**
     * Finds a category by its unique ID.
     *
     * @param id the category ID to look up
     * @return an {@link Optional} containing the category if found, or empty if not
     */
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(categories.get(id));
    }

    /**
     * Returns all categories in the store.
     *
     * <p>Returns a defensive copy to prevent external modification of the internal map.</p>
     *
     * @return a list of all categories; may be empty but never {@code null}
     */
    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    /**
     * Checks whether a category with the given name already exists (case-insensitive).
     *
     * <p>Used during category creation to prevent duplicates.</p>
     *
     * @param name the category name to check
     * @return {@code true} if a category with this name exists
     */
    public boolean existsByName(String name) {
        return categories.values().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }

    /**
     * Checks whether a category with the given name exists, excluding a specific category ID.
     *
     * <p>Used during category update to allow renaming a category to its current name
     * while still preventing conflicts with other categories.</p>
     *
     * @param name the category name to check
     * @param id   the category ID to exclude from the check
     * @return {@code true} if another category with this name exists
     */
    public boolean existsByNameAndNotId(String name, Long id) {
        return categories.values().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name) && !c.getId().equals(id));
    }

    /**
     * Deletes a category by its ID.
     *
     * <p><strong>Warning:</strong> Does not check for referencing books. The caller
     * ({@link com.toptal.bookshopv2.service.CategoryService}) must verify this constraint.</p>
     *
     * @param id the ID of the category to delete
     */
    public void deleteById(Long id) {
        categories.remove(id);
    }
}

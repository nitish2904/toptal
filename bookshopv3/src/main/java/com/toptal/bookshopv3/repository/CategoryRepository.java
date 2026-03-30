package com.toptal.bookshopv3.repository;

import com.toptal.bookshopv3.model.Category;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CategoryRepository {
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final ConcurrentHashMap<Long, Category> categories = new ConcurrentHashMap<>();

    public Category save(Category category) {
        if (category.getId() == null) category.setId(idGenerator.incrementAndGet());
        categories.put(category.getId(), category);
        return category;
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(categories.get(id));
    }

    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    public boolean existsByName(String name) {
        return categories.values().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }

    public boolean existsByNameAndNotId(String name, Long id) {
        return categories.values().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name) && !c.getId().equals(id));
    }

    public void deleteById(Long id) {
        categories.remove(id);
    }
}

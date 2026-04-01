package com.toptal.bookshopv3.repository;

import com.toptal.bookshopv3.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
/** Spring Data JPA repository for {@link com.toptal.bookshopv3.entity.Book} with custom in-stock and locking queries. */

@Repository
public class BookRepository {
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final ConcurrentHashMap<Long, Book> books = new ConcurrentHashMap<>();

    public Book save(Book book) {
        if (book.getId() == null) book.setId(idGenerator.incrementAndGet());
        books.put(book.getId(), book);
        return book;
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    public List<Book> findInStock() {
        return books.values().stream()
                .filter(b -> b.getStock() != null && b.getStock() > 0)
                .collect(Collectors.toList());
    }

    public List<Book> findInStockByCategoryIds(List<Long> categoryIds) {
        return books.values().stream()
                .filter(b -> b.getStock() != null && b.getStock() > 0)
                .filter(b -> categoryIds.contains(b.getCategoryId()))
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        books.remove(id);
    }

    public boolean existsByCategoryId(Long categoryId) {
        return books.values().stream()
                .anyMatch(b -> categoryId.equals(b.getCategoryId()));
    }

    public Map<Long, Book> getBookMap() {
        return Collections.unmodifiableMap(books);
    }
}

package com.toptal.bookshopv2.service;

import com.toptal.bookshopv2.dto.*;
import com.toptal.bookshopv2.exception.ResourceNotFoundException;
import com.toptal.bookshopv2.model.Book;
import com.toptal.bookshopv2.model.Category;
import com.toptal.bookshopv2.repository.BookRepository;
import com.toptal.bookshopv2.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

/**
 * Service for book catalog operations: listing, searching, CRUD, and sorting.
 *
 * <p>Supports filtering by category, multi-field sorting, and pagination.
 * Only in-stock books ({@code stock > 0}) are returned in catalog queries.</p>
 *
 * @author Nitish
 * @version 2.0
 * @see BookRepository
 * @see com.toptal.bookshopv2.controller.BookController
 */
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    /** Returns a paginated, sorted list of in-stock books, optionally filtered by category IDs. */
    public List<BookResponse> getBooks(List<Long> categoryIds, String sortBy, String sortDir, int page, int size) {
        List<Book> list = (categoryIds != null && !categoryIds.isEmpty())
                ? bookRepository.findInStockByCategoryIds(categoryIds) : bookRepository.findInStock();
        Comparator<Book> cmp = getComparator(sortBy != null ? sortBy : "id");
        if ("desc".equalsIgnoreCase(sortDir)) cmp = cmp.reversed();
        list = list.stream().sorted(cmp).toList();
        int start = Math.min(page * size, list.size());
        int end = Math.min(start + size, list.size());
        return list.subList(start, end).stream()
                .map(b -> BookResponse.from(b, categoryRepository.findById(b.getCategoryId()).orElse(null))).toList();
    }
    /** Returns the total count of in-stock books, optionally filtered by category IDs. */
    public long countBooks(List<Long> categoryIds) {
        return (categoryIds != null && !categoryIds.isEmpty())
                ? bookRepository.findInStockByCategoryIds(categoryIds).size() : bookRepository.findInStock().size();
    }
    /** Returns a single book by ID, including out-of-stock books. */
    public BookResponse getBookById(Long id) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookResponse.from(b, categoryRepository.findById(b.getCategoryId()).orElse(null));
    }
    /** Creates a new book (admin only). Validates that the category exists. */
    public BookResponse createBook(BookRequest req) {
        categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
        Book b = bookRepository.save(Book.builder().title(req.getTitle()).author(req.getAuthor())
                .yearPublished(req.getYearPublished()).price(req.getPrice()).stock(req.getStock())
                .categoryId(req.getCategoryId()).build());
        return BookResponse.from(b, categoryRepository.findById(b.getCategoryId()).orElse(null));
    }
    /** Updates an existing book's metadata (admin only). Does not modify stock. */
    public BookResponse updateBook(Long id, BookUpdateRequest req) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
        b.setTitle(req.getTitle()); b.setAuthor(req.getAuthor());
        b.setYearPublished(req.getYearPublished()); b.setPrice(req.getPrice()); b.setCategoryId(req.getCategoryId());
        bookRepository.save(b);
        return BookResponse.from(b, categoryRepository.findById(b.getCategoryId()).orElse(null));
    }
    /** Deletes a book by ID (admin only). */
    public void deleteBook(Long id) {
        if (bookRepository.findById(id).isEmpty()) throw new ResourceNotFoundException("Book not found with id: " + id);
        bookRepository.deleteById(id);
    }
    /** Returns a comparator for the given sort field (title, author, price, year, or id). */
    private Comparator<Book> getComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "title" -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "author" -> Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparing(Book::getPrice);
            case "yearpublished", "year" -> Comparator.comparing(Book::getYearPublished);
            default -> Comparator.comparing(Book::getId);
        };
    }
}

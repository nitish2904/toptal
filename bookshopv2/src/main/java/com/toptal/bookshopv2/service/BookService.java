package com.toptal.bookshopv2.service;

import com.toptal.bookshopv2.dto.*;
import com.toptal.bookshopv2.exception.ResourceNotFoundException;
import com.toptal.bookshopv2.model.Book;
import com.toptal.bookshopv2.model.Category;
import com.toptal.bookshopv2.store.DataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final DataStore dataStore;

    public List<BookResponse> getBooks(List<Long> categoryIds, String sortBy, String sortDir, int page, int size) {
        List<Book> list = (categoryIds != null && !categoryIds.isEmpty())
                ? dataStore.findInStockBooksByCategoryIds(categoryIds) : dataStore.findInStockBooks();
        Comparator<Book> cmp = getComparator(sortBy != null ? sortBy : "id");
        if ("desc".equalsIgnoreCase(sortDir)) cmp = cmp.reversed();
        list = list.stream().sorted(cmp).toList();
        int start = Math.min(page * size, list.size());
        int end = Math.min(start + size, list.size());
        return list.subList(start, end).stream()
                .map(b -> BookResponse.from(b, dataStore.findCategoryById(b.getCategoryId()).orElse(null))).toList();
    }
    public long countBooks(List<Long> categoryIds) {
        return (categoryIds != null && !categoryIds.isEmpty())
                ? dataStore.findInStockBooksByCategoryIds(categoryIds).size() : dataStore.findInStockBooks().size();
    }
    public BookResponse getBookById(Long id) {
        Book b = dataStore.findBookById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookResponse.from(b, dataStore.findCategoryById(b.getCategoryId()).orElse(null));
    }
    public BookResponse createBook(BookRequest req) {
        dataStore.findCategoryById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
        Book b = dataStore.saveBook(Book.builder().title(req.getTitle()).author(req.getAuthor())
                .yearPublished(req.getYearPublished()).price(req.getPrice()).stock(req.getStock())
                .categoryId(req.getCategoryId()).build());
        return BookResponse.from(b, dataStore.findCategoryById(b.getCategoryId()).orElse(null));
    }
    public BookResponse updateBook(Long id, BookUpdateRequest req) {
        Book b = dataStore.findBookById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        dataStore.findCategoryById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
        b.setTitle(req.getTitle()); b.setAuthor(req.getAuthor());
        b.setYearPublished(req.getYearPublished()); b.setPrice(req.getPrice()); b.setCategoryId(req.getCategoryId());
        dataStore.saveBook(b);
        return BookResponse.from(b, dataStore.findCategoryById(b.getCategoryId()).orElse(null));
    }
    public void deleteBook(Long id) {
        if (dataStore.findBookById(id).isEmpty()) throw new ResourceNotFoundException("Book not found with id: " + id);
        dataStore.deleteBook(id);
    }
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

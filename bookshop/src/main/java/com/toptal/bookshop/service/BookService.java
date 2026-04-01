package com.toptal.bookshop.service;

import com.toptal.bookshop.dto.BookRequest;
import com.toptal.bookshop.dto.BookResponse;
import com.toptal.bookshop.dto.BookUpdateRequest;
import com.toptal.bookshop.entity.Book;
import com.toptal.bookshop.entity.Category;
import com.toptal.bookshop.exception.ResourceNotFoundException;
import com.toptal.bookshop.repository.BookRepository;
import com.toptal.bookshop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/** Service for browsing, creating, updating, and deleting books (admin). */

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    /**
     * List books in stock. Optionally filter by category IDs.
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> getBooks(List<Long> categoryIds, Pageable pageable) {
        Page<Book> books;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            books = bookRepository.findInStockByCategoryIds(categoryIds, pageable);
        } else {
            books = bookRepository.findByStockGreaterThan(0, pageable);
        }
        return books.map(BookResponse::from);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookResponse.from(book);
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .yearPublished(request.getYearPublished())
                .price(request.getPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .category(category)
                .build();

        book = bookRepository.save(book);
        return BookResponse.from(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setYearPublished(request.getYearPublished());
        book.setPrice(request.getPrice());
        book.setCategory(category);
        // Note: stock is NOT updatable

        book = bookRepository.save(book);
        return BookResponse.from(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
}

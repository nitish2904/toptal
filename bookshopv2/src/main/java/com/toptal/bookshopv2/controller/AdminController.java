package com.toptal.bookshopv2.controller;

import com.toptal.bookshopv2.dto.*;
import com.toptal.bookshopv2.service.BookService;
import com.toptal.bookshopv2.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin-only operations on categories and books.
 *
 * <p>All endpoints require ADMIN role. Provides CRUD for categories and books.</p>
 *
 * @author Nitish
 * @version 2.0
 */
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminController {
    private final CategoryService categoryService;
    private final BookService bookService;

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id); return ResponseEntity.noContent().build();
    }
    @PostMapping("/books")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }
    @PutMapping("/books/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id); return ResponseEntity.noContent().build();
    }
}

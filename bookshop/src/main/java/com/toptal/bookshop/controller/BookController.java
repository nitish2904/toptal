package com.toptal.bookshop.controller;

import com.toptal.bookshop.dto.BookResponse;
import com.toptal.bookshop.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/** REST controller exposing public book browsing and admin book management endpoints. */

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Public endpoint: list in-stock books with optional category filtering.
     * Supports pagination: ?page=0&size=20&sort=title,asc
     * Supports filtering: ?category=1,2,3
     */
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getBooks(
            @RequestParam(required = false) List<Long> category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooks(category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}

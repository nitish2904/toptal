package com.toptal.bookshopv3.controller;

import com.toptal.bookshopv3.dto.BookResponse;
import com.toptal.bookshopv3.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/books") @RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBooks(
            @RequestParam(required = false) List<Long> category,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (size > 100) size = 100;
        if (size < 1) size = 20;
        if (page < 0) page = 0;
        List<BookResponse> books = bookService.getBooks(category, sortBy, sortDir, page, size);
        long totalElements = bookService.countBooks(category);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        Map<String, Object> response = new HashMap<>();
        response.put("content", books);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("size", size);
        response.put("number", page);
        response.put("empty", books.isEmpty());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}

package com.toptal.bookshopv2.controller;

import com.toptal.bookshopv2.dto.CategoryResponse;
import com.toptal.bookshopv2.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for public category browsing.
 *
 * <p>Provides read-only access to the category catalog. No authentication required.</p>
 *
 * @author Nitish
 * @version 2.0
 */
@RestController @RequestMapping("/api/categories") @RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
}

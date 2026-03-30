package com.toptal.bookshopv2.service;

import com.toptal.bookshopv2.dto.CategoryRequest;
import com.toptal.bookshopv2.dto.CategoryResponse;
import com.toptal.bookshopv2.exception.*;
import com.toptal.bookshopv2.model.Category;
import com.toptal.bookshopv2.repository.BookRepository;
import com.toptal.bookshopv2.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }
    public CategoryResponse getCategoryById(Long id) {
        return CategoryResponse.from(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id)));
    }
    public CategoryResponse createCategory(CategoryRequest req) {
        if (categoryRepository.existsByName(req.getName()))
            throw new ConflictException("Category already exists: " + req.getName());
        return CategoryResponse.from(categoryRepository.save(Category.builder().name(req.getName()).build()));
    }
    public CategoryResponse updateCategory(Long id, CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (categoryRepository.existsByNameAndNotId(req.getName(), id))
            throw new ConflictException("Category already exists: " + req.getName());
        c.setName(req.getName());
        return CategoryResponse.from(categoryRepository.save(c));
    }
    public void deleteCategory(Long id) {
        if (categoryRepository.findById(id).isEmpty())
            throw new ResourceNotFoundException("Category not found with id: " + id);
        if (bookRepository.existsByCategoryId(id))
            throw new BadRequestException("Cannot delete category that has books. Remove the books first.");
        categoryRepository.deleteById(id);
    }
}

package com.toptal.bookshopv2.service;

import com.toptal.bookshopv2.dto.CategoryRequest;
import com.toptal.bookshopv2.dto.CategoryResponse;
import com.toptal.bookshopv2.exception.*;
import com.toptal.bookshopv2.model.Category;
import com.toptal.bookshopv2.store.DataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final DataStore dataStore;

    public List<CategoryResponse> getAllCategories() {
        return dataStore.findAllCategories().stream().map(CategoryResponse::from).toList();
    }
    public CategoryResponse getCategoryById(Long id) {
        return CategoryResponse.from(dataStore.findCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id)));
    }
    public CategoryResponse createCategory(CategoryRequest req) {
        if (dataStore.existsCategoryByName(req.getName()))
            throw new ConflictException("Category already exists: " + req.getName());
        return CategoryResponse.from(dataStore.saveCategory(Category.builder().name(req.getName()).build()));
    }
    public CategoryResponse updateCategory(Long id, CategoryRequest req) {
        Category c = dataStore.findCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (dataStore.existsCategoryByNameAndNotId(req.getName(), id))
            throw new ConflictException("Category already exists: " + req.getName());
        c.setName(req.getName());
        return CategoryResponse.from(dataStore.saveCategory(c));
    }
    public void deleteCategory(Long id) {
        if (dataStore.findCategoryById(id).isEmpty())
            throw new ResourceNotFoundException("Category not found with id: " + id);
        if (dataStore.existsBookByCategoryId(id))
            throw new BadRequestException("Cannot delete category that has books. Remove the books first.");
        dataStore.deleteCategory(id);
    }
}

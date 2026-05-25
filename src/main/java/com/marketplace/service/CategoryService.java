package com.marketplace.service;

import com.marketplace.entity.Category;
import com.marketplace.exception.NotFoundException;
import com.marketplace.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> activeCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    public Category requireActiveBySlug(String slug) {
        return categoryRepository.findBySlugAndActiveTrue(slug)
            .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
    }

    public Category requireById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
    }
}

package com.marketplace.service;

import com.marketplace.dto.CategoryForm;
import com.marketplace.entity.Category;
import com.marketplace.exception.NotFoundException;
import com.marketplace.repository.CategoryRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.util.SlugUtil;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> activeCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    public List<Category> adminCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder").and(Sort.by("name")));
    }

    public Category requireActiveBySlug(String slug) {
        return categoryRepository.findBySlugAndActiveTrue(slug)
            .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
    }

    public Category requireById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
    }

    public CategoryForm toForm(Category category) {
        CategoryForm form = new CategoryForm();
        form.setName(category.getName());
        form.setSlug(category.getSlug());
        form.setIcon(category.getIcon());
        form.setActive(category.isActive());
        form.setDisplayOrder(category.getDisplayOrder());
        return form;
    }

    @Transactional
    public Category create(CategoryForm form) {
        Category category = new Category();
        applyForm(category, form);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, CategoryForm form) {
        Category category = requireById(id);
        applyForm(category, form);
        return category;
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Category category = requireById(id);
        category.setActive(active);
    }

    @Transactional
    public void deleteOrDeactivate(Long id) {
        Category category = requireById(id);
        if (productRepository.countByCategoryId(id) > 0) {
            category.setActive(false);
            return;
        }
        categoryRepository.delete(category);
    }

    public long productCount(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    private void applyForm(Category category, CategoryForm form) {
        category.setName(form.getName().trim());
        category.setSlug(uniqueSlug(form.getSlug(), form.getName(), category.getId()));
        category.setIcon(blankToNull(form.getIcon()));
        category.setActive(form.isActive());
        category.setDisplayOrder(form.getDisplayOrder());
    }

    private String uniqueSlug(String requestedSlug, String name, Long currentId) {
        String base = SlugUtil.slugify(requestedSlug == null || requestedSlug.isBlank() ? name : requestedSlug);
        String candidate = base;
        int suffix = 2;
        while (categoryRepository.findBySlug(candidate)
            .filter(existing -> currentId == null || !existing.getId().equals(currentId))
            .isPresent()) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

package com.marketplace.controller.admin;

import com.marketplace.dto.CategoryForm;
import com.marketplace.service.CategoryService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        var categories = categoryService.adminCategories();
        Map<Long, Long> productCounts = categories.stream()
            .collect(Collectors.toMap(category -> category.getId(), category -> categoryService.productCount(category.getId())));
        model.addAttribute("adminCategories", categories);
        model.addAttribute("productCounts", productCounts);
        return "admin/categories";
    }

    @GetMapping("/admin/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("formAction", "/admin/categories/new");
        model.addAttribute("formTitle", "Nueva categoría");
        return "admin/category-form";
    }

    @PostMapping("/admin/categories/new")
    public String create(@Valid CategoryForm categoryForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/admin/categories/new");
            model.addAttribute("formTitle", "Nueva categoría");
            return "admin/category-form";
        }
        categoryService.create(categoryForm);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("categoryForm", categoryService.toForm(categoryService.requireById(id)));
        model.addAttribute("formAction", "/admin/categories/" + id + "/edit");
        model.addAttribute("formTitle", "Editar categoría");
        return "admin/category-form";
    }

    @PostMapping("/admin/categories/{id}/edit")
    public String update(@PathVariable Long id, @Valid CategoryForm categoryForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/admin/categories/" + id + "/edit");
            model.addAttribute("formTitle", "Editar categoría");
            return "admin/category-form";
        }
        categoryService.update(id, categoryForm);
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{id}/activate")
    public String activate(@PathVariable Long id) {
        categoryService.setActive(id, true);
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{id}/deactivate")
    public String deactivate(@PathVariable Long id) {
        categoryService.setActive(id, false);
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{id}/delete")
    public String delete(@PathVariable Long id) {
        categoryService.deleteOrDeactivate(id);
        return "redirect:/admin/categories";
    }
}

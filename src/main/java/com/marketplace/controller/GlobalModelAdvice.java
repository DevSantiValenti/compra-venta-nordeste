package com.marketplace.controller;

import com.marketplace.config.AppProperties;
import com.marketplace.service.CategoryService;
import com.marketplace.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;
    private final AppProperties appProperties;
    private final Environment environment;

    public GlobalModelAdvice(CategoryService categoryService, CurrentUserService currentUserService, AppProperties appProperties, Environment environment) {
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
        this.appProperties = appProperties;
        this.environment = environment;
    }

    @ModelAttribute("categories")
    Object categories() {
        return categoryService.activeCategories();
    }

    @ModelAttribute("currentUser")
    Object currentUser() {
        return currentUserService.currentUser().orElse(null);
    }

    @ModelAttribute("siteName")
    String siteName() {
        return appProperties.siteName();
    }

    @ModelAttribute("googleOAuthEnabled")
    boolean googleOAuthEnabled() {
        return environment.getProperty("spring.security.oauth2.client.registration.google.client-id") != null;
    }

    @ModelAttribute("currentPath")
    String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}

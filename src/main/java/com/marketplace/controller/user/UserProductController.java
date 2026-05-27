package com.marketplace.controller.user;

import com.marketplace.dto.ProductForm;
import com.marketplace.dto.ProfileForm;
import com.marketplace.entity.Product;
import com.marketplace.entity.ProductCurrency;
import com.marketplace.entity.ProductStatus;
import com.marketplace.entity.User;
import com.marketplace.repository.ProductRepository;
import com.marketplace.service.CurrentUserService;
import com.marketplace.service.FavoriteService;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserProductController {
    private final CurrentUserService currentUserService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final FavoriteService favoriteService;

    public UserProductController(CurrentUserService currentUserService, ProductService productService, ProductRepository productRepository, UserService userService, FavoriteService favoriteService) {
        this.currentUserService = currentUserService;
        this.productService = productService;
        this.productRepository = productRepository;
        this.userService = userService;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/user/dashboard")
    public String dashboard(Model model) {
        User user = currentUserService.requireUser();
        model.addAttribute("activeCount", productRepository.countBySellerAndStatus(user, ProductStatus.ACTIVE));
        model.addAttribute("soldCount", productRepository.countBySellerAndStatus(user, ProductStatus.SOLD));
        model.addAttribute("totalViews", productRepository.sumViewsBySeller(user));
        model.addAttribute("totalWhatsapp", productRepository.sumWhatsappClicksBySeller(user));
        model.addAttribute("products", productService.mine(user, PageRequest.of(0, 20)));
        return "user/dashboard";
    }

    @GetMapping("/user/favorites")
    public String favorites(Model model) {
        User user = currentUserService.requireUser();
        model.addAttribute("products", favoriteService.productsFor(user));
        return "user/favorites";
    }

    @PostMapping("/user/favorites/{slug}")
    @ResponseBody
    public Map<String, Boolean> addFavorite(@PathVariable String slug) {
        favoriteService.add(currentUserService.requireUser(), slug);
        return Map.of("favorite", true);
    }

    @DeleteMapping("/user/favorites/{slug}")
    @ResponseBody
    public Map<String, Boolean> removeFavorite(@PathVariable String slug) {
        favoriteService.remove(currentUserService.requireUser(), slug);
        return Map.of("favorite", false);
    }

    @GetMapping("/user/profile")
    public String profile(Model model) {
        User user = currentUserService.requireUser();
        ProfileForm form = new ProfileForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setPhone(user.getPhone());
        form.setCity(user.getCity());
        form.setProvince(user.getProvince());
        model.addAttribute("profileForm", form);
        return "user/profile";
    }

    @PostMapping("/user/profile")
    public String updateProfile(@Valid ProfileForm profileForm, BindingResult bindingResult, @RequestParam(value = "avatar", required = false) MultipartFile avatar, Model model) {
        if (bindingResult.hasErrors()) {
            return "user/profile";
        }
        try {
            userService.updateProfile(currentUserService.requireUser(), profileForm, avatar);
            return "redirect:/user/profile?saved";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("avatarError", ex.getMessage());
            return "user/profile";
        }
    }

    @PostMapping("/user/profile/avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile avatar, Model model) {
        try {
            userService.updateAvatar(currentUserService.requireUser(), avatar);
            return "redirect:/user/profile?avatarSaved";
        } catch (IllegalArgumentException ex) {
            User user = currentUserService.requireUser();
            ProfileForm form = new ProfileForm();
            form.setFirstName(user.getFirstName());
            form.setLastName(user.getLastName());
            form.setPhone(user.getPhone());
            form.setCity(user.getCity());
            form.setProvince(user.getProvince());
            model.addAttribute("profileForm", form);
            model.addAttribute("avatarError", ex.getMessage());
            return "user/profile";
        }
    }

    @GetMapping("/products/my")
    public String myProducts(Model model) {
        model.addAttribute("products", productService.mine(currentUserService.requireUser(), PageRequest.of(0, 30)));
        return "user/my-products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        User user = currentUserService.requireUser();
        ProductForm form = new ProductForm();
        form.setCity(user.getCity());
        form.setProvince(user.getProvince());
        model.addAttribute("productForm", form);
        model.addAttribute("formAction", "/products/new");
        return "user/product-form";
    }

    @PostMapping("/products/new")
    public String create(@Valid ProductForm productForm, BindingResult bindingResult, @RequestParam("images") MultipartFile[] images, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/products/new");
            return "user/product-form";
        }
        try {
            Product product = productService.create(productForm, images, currentUserService.requireUser());
            return "redirect:/producto/" + product.getSlug();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("formAction", "/products/new");
            model.addAttribute("imageError", ex.getMessage());
            return "user/product-form";
        }
    }

    @GetMapping("/products/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Product product = productService.getOwned(id, currentUserService.requireUser());
        ProductForm form = new ProductForm();
        form.setTitle(product.getTitle());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setCurrency(product.getCurrency() == null ? ProductCurrency.ARS : product.getCurrency());
        form.setCondition(product.getCondition());
        form.setBrand(product.getBrand());
        form.setSize(product.getSize());
        form.setWheelSize(product.getWheelSize());
        form.setCity(product.getCity());
        form.setProvince(product.getProvince());
        form.setCategoryId(product.getCategory().getId());
        model.addAttribute("productForm", form);
        model.addAttribute("product", product);
        model.addAttribute("formAction", "/products/edit/" + id);
        return "user/product-form";
    }

    @PostMapping("/products/edit/{id}")
    public String update(@PathVariable Long id, @Valid ProductForm productForm, BindingResult bindingResult, @RequestParam("images") MultipartFile[] images, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/products/edit/" + id);
            model.addAttribute("product", productService.getOwned(id, currentUserService.requireUser()));
            return "user/product-form";
        }
        try {
            Product product = productService.update(id, productForm, images, currentUserService.requireUser());
            return "redirect:/producto/" + product.getSlug();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("formAction", "/products/edit/" + id);
            model.addAttribute("product", productService.getOwned(id, currentUserService.requireUser()));
            model.addAttribute("imageError", ex.getMessage());
            return "user/product-form";
        }
    }

    @PostMapping("/products/{id}/sold")
    public String sold(@PathVariable Long id, @RequestParam(defaultValue = "/products/my") String returnTo) {
        productService.markSold(id, currentUserService.requireUser());
        return "redirect:" + safeReturnTo(returnTo);
    }

    @PostMapping("/products/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, @RequestParam(defaultValue = "/products/my") String returnTo) {
        productService.toggleActive(id, currentUserService.requireUser());
        return "redirect:" + safeReturnTo(returnTo);
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(defaultValue = "/products/my") String returnTo) {
        productService.deactivate(id, currentUserService.requireUser());
        return "redirect:" + safeReturnTo(returnTo);
    }

    private String safeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/products/my";
        }
        return returnTo;
    }
}

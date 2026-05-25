package com.marketplace.controller.admin;

import com.marketplace.entity.ProductStatus;
import com.marketplace.entity.ReportStatus;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ReportRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.AdminStatsService;
import com.marketplace.service.CurrentUserService;
import com.marketplace.service.ProductService;
import com.marketplace.service.ReportService;
import com.marketplace.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {
    private final AdminStatsService adminStatsService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final CurrentUserService currentUserService;

    public AdminController(
        AdminStatsService adminStatsService,
        UserRepository userRepository,
        UserService userService,
        ProductRepository productRepository,
        ProductService productService,
        ReportRepository reportRepository,
        ReportService reportService,
        CurrentUserService currentUserService
    ) {
        this.adminStatsService = adminStatsService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.productRepository = productRepository;
        this.productService = productService;
        this.reportRepository = reportRepository;
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/admin")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminStatsService.stats());
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.get(id));
        return "admin/user-detail";
    }

    @PostMapping("/admin/users/{id}/block")
    public String block(@PathVariable Long id) {
        userService.setBlocked(id, true);
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/admin/users/{id}/unblock")
    public String unblock(@PathVariable Long id) {
        userService.setBlocked(id, false);
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/admin/users/{id}/verify")
    public String verify(@PathVariable Long id) {
        userService.verify(id);
        return "redirect:/admin/users/" + id;
    }

    @GetMapping("/admin/products")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))));
        return "admin/products";
    }

    @GetMapping("/admin/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productRepository.findById(id).orElseThrow());
        return "admin/product-detail";
    }

    @PostMapping("/admin/products/{id}/hide")
    public String hideProduct(@PathVariable Long id) {
        productService.adminSetStatus(id, ProductStatus.HIDDEN);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/reactivate")
    public String reactivateProduct(@PathVariable Long id) {
        productService.adminSetStatus(id, ProductStatus.ACTIVE);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.adminSetStatus(id, ProductStatus.DELETED);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/reports")
    public String reports(Model model) {
        model.addAttribute("reports", reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING));
        return "admin/reports";
    }

    @GetMapping("/admin/reports/{id}")
    public String reportDetail(@PathVariable Long id, Model model) {
        model.addAttribute("report", reportService.get(id));
        return "admin/report-detail";
    }

    @PostMapping("/admin/reports/{id}/approve")
    public String approveReport(@PathVariable Long id, @RequestParam(required = false) String adminComment, @RequestParam(required = false) Boolean blockUser) {
        reportService.approve(id, currentUserService.requireUser(), adminComment, Boolean.TRUE.equals(blockUser));
        return "redirect:/admin/reports";
    }

    @PostMapping("/admin/reports/{id}/reject")
    public String rejectReport(@PathVariable Long id, @RequestParam(required = false) String adminComment) {
        reportService.reject(id, currentUserService.requireUser(), adminComment);
        return "redirect:/admin/reports";
    }
}

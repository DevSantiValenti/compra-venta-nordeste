package com.marketplace.controller.publicsite;

import com.marketplace.config.AppProperties;
import com.marketplace.dto.ProductSearch;
import com.marketplace.dto.RegisterForm;
import com.marketplace.dto.ReportForm;
import com.marketplace.entity.Category;
import com.marketplace.entity.Product;
import com.marketplace.entity.ProductStatus;
import com.marketplace.repository.ProductRepository;
import com.marketplace.service.CategoryService;
import com.marketplace.service.CurrentUserService;
import com.marketplace.service.FavoriteService;
import com.marketplace.service.MetricsService;
import com.marketplace.service.ProductService;
import com.marketplace.service.ReportService;
import com.marketplace.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PublicController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final MetricsService metricsService;
    private final ReportService reportService;
    private final CurrentUserService currentUserService;
    private final ProductRepository productRepository;
    private final AppProperties appProperties;
    private final FavoriteService favoriteService;

    public PublicController(
        ProductService productService,
        CategoryService categoryService,
        UserService userService,
        MetricsService metricsService,
        ReportService reportService,
        CurrentUserService currentUserService,
        ProductRepository productRepository,
        AppProperties appProperties,
        FavoriteService favoriteService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.metricsService = metricsService;
        this.reportService = reportService;
        this.currentUserService = currentUserService;
        this.productRepository = productRepository;
        this.appProperties = appProperties;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/")
    public String home(@ModelAttribute ProductSearch search, Model model) {
        model.addAttribute("products", productService.search(search, PageRequest.of(0, 24)));
        model.addAttribute("search", search);
        model.addAttribute("metaTitle", appProperties.siteName() + " | Bicis y componentes del Nordeste");
        model.addAttribute("metaDescription", "Comprá y vendé bicicletas, componentes, indumentaria y accesorios de ciclismo en el Nordeste Argentino.");
        return "public/home";
    }

    @GetMapping("/categoria/{slug}")
    public String category(@PathVariable String slug, Model model) {
        Category category = categoryService.requireActiveBySlug(slug);
        model.addAttribute("category", category);
        model.addAttribute("products", productService.byCategory(slug, PageRequest.of(0, 24)));
        model.addAttribute("metaTitle", category.getName() + " | " + appProperties.siteName());
        model.addAttribute("metaDescription", "Publicaciones activas en " + category.getName() + " para ciclistas del Nordeste Argentino.");
        return "public/category";
    }

    @GetMapping("/producto/{slug}")
    public String product(@PathVariable String slug, Model model, HttpServletRequest request) {
        Product product = productService.visibleBySlug(slug);
        metricsService.recordProductView(product, request);
        model.addAttribute("product", product);
        model.addAttribute("reportForm", new ReportForm());
        model.addAttribute("metaTitle", product.getTitle() + " | " + appProperties.siteName());
        model.addAttribute("metaDescription", product.getDescription().length() > 150 ? product.getDescription().substring(0, 150) : product.getDescription());
        model.addAttribute("ogImage", product.getMainImage() == null ? "" : product.getMainImage().getFilePath());
        model.addAttribute("favoriteActive", currentUserService.currentUser()
            .map(user -> favoriteService.isFavorite(user, product))
            .orElse(false));
        return "public/product-detail";
    }

    @GetMapping("/producto/{slug}/whatsapp")
    public String whatsapp(@PathVariable String slug, HttpServletRequest request) {
        Product product = productService.activeBySlug(slug);
        return "redirect:" + metricsService.recordWhatsappClick(product, request, appProperties.siteName());
    }

    @PostMapping("/producto/{slug}/report")
    public String reportProduct(@PathVariable String slug, @Valid ReportForm form, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            Product product = productService.activeBySlug(slug);
            reportService.reportProduct(product, currentUserService.currentUser().orElse(null), form);
        }
        return "redirect:/producto/" + slug;
    }

    @PostMapping("/users/{id}/report")
    public String reportUser(@PathVariable Long id, @Valid ReportForm form) {
        reportService.reportUser(id, currentUserService.currentUser().orElse(null), form);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "public/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "public/register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterForm registerForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "public/register";
        }
        try {
            userService.register(registerForm);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registerError", ex.getMessage());
            return "public/register";
        }
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return "User-agent: *\nAllow: /\nSitemap: /sitemap.xml\n";
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
            .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
            .forEach(product -> xml.append("<url><loc>")
                .append(baseUrl).append("/producto/").append(product.getSlug())
                .append("</loc><lastmod>")
                .append(product.getUpdatedAt().toLocalDate().format(DateTimeFormatter.ISO_DATE))
                .append("</lastmod></url>\n"));
        xml.append("</urlset>");
        return xml.toString();
    }
}

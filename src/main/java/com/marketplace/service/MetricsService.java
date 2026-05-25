package com.marketplace.service;

import com.marketplace.entity.Product;
import com.marketplace.entity.ProductViewEvent;
import com.marketplace.entity.SiteVisitEvent;
import com.marketplace.entity.User;
import com.marketplace.entity.WhatsappClickEvent;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ProductViewEventRepository;
import com.marketplace.repository.SiteVisitEventRepository;
import com.marketplace.repository.WhatsappClickEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsService {
    private final ProductRepository productRepository;
    private final ProductViewEventRepository productViewEventRepository;
    private final WhatsappClickEventRepository whatsappClickEventRepository;
    private final SiteVisitEventRepository siteVisitEventRepository;
    private final CurrentUserService currentUserService;

    public MetricsService(
        ProductRepository productRepository,
        ProductViewEventRepository productViewEventRepository,
        WhatsappClickEventRepository whatsappClickEventRepository,
        SiteVisitEventRepository siteVisitEventRepository,
        CurrentUserService currentUserService
    ) {
        this.productRepository = productRepository;
        this.productViewEventRepository = productViewEventRepository;
        this.whatsappClickEventRepository = whatsappClickEventRepository;
        this.siteVisitEventRepository = siteVisitEventRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void recordProductView(Product product, HttpServletRequest request) {
        product.setViewsCount(product.getViewsCount() + 1);
        ProductViewEvent event = new ProductViewEvent();
        event.setProduct(product);
        event.setViewerUser(currentUserService.currentUser().orElse(null));
        event.setIpAddress(request.getRemoteAddr());
        event.setUserAgent(request.getHeader("User-Agent"));
        productViewEventRepository.save(event);
    }

    @Transactional
    public String recordWhatsappClick(Product product, HttpServletRequest request, String siteName) {
        product.setWhatsappClicksCount(product.getWhatsappClicksCount() + 1);
        WhatsappClickEvent event = new WhatsappClickEvent();
        event.setProduct(product);
        event.setUser(currentUserService.currentUser().orElse(null));
        event.setIpAddress(request.getRemoteAddr());
        event.setUserAgent(request.getHeader("User-Agent"));
        whatsappClickEventRepository.save(event);
        String message = "Hola, vi tu publicación de " + product.getTitle() + " en " + siteName + ". ¿Sigue disponible?";
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "https://wa.me/" + product.getSeller().getPhone().replaceAll("[^0-9]", "") + "?text=" + encoded;
    }

    @Transactional
    public void recordSiteVisit(HttpServletRequest request) {
        if (request.getRequestURI().contains(".")) {
            return;
        }
        SiteVisitEvent event = new SiteVisitEvent();
        event.setPath(request.getRequestURI());
        event.setIpAddress(request.getRemoteAddr());
        event.setUserAgent(request.getHeader("User-Agent"));
        siteVisitEventRepository.save(event);
    }

    public long totalProductViews() {
        return productRepository.sumViewsCount();
    }

    public long totalWhatsappClicks() {
        return productRepository.sumWhatsappClicksCount();
    }
}

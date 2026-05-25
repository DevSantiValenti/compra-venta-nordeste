package com.marketplace.service;

import com.marketplace.entity.ProductStatus;
import com.marketplace.entity.ReportStatus;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ReportRepository;
import com.marketplace.repository.SiteVisitEventRepository;
import com.marketplace.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AdminStatsService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReportRepository reportRepository;
    private final SiteVisitEventRepository siteVisitEventRepository;

    public AdminStatsService(
        UserRepository userRepository,
        ProductRepository productRepository,
        ReportRepository reportRepository,
        SiteVisitEventRepository siteVisitEventRepository
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reportRepository = reportRepository;
        this.siteVisitEventRepository = siteVisitEventRepository;
    }

    public AdminStats stats() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime week = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime month = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        LocalDateTime now = LocalDateTime.now();
        return new AdminStats(
            userRepository.count(),
            userRepository.countByCreatedAtBetween(today, tomorrow),
            userRepository.countByCreatedAtBetween(week, now),
            userRepository.countByCreatedAtBetween(month, now),
            productRepository.count(),
            productRepository.countByCreatedAtBetween(today, tomorrow),
            productRepository.countByCreatedAtBetween(week, now),
            productRepository.countByCreatedAtBetween(month, now),
            productRepository.countByStatus(ProductStatus.ACTIVE),
            productRepository.countByStatus(ProductStatus.HIDDEN),
            productRepository.countByStatus(ProductStatus.SOLD),
            productRepository.countByFeaturedTrue(),
            siteVisitEventRepository.count(),
            siteVisitEventRepository.countByCreatedAtBetween(today, tomorrow),
            siteVisitEventRepository.countByCreatedAtBetween(week, now),
            siteVisitEventRepository.countByCreatedAtBetween(month, now),
            productRepository.sumViewsCount(),
            productRepository.sumWhatsappClicksCount(),
            reportRepository.countByStatus(ReportStatus.PENDING),
            userRepository.countByBlockedTrue(),
            productRepository.findTop10ByStatusOrderByViewsCountDesc(ProductStatus.ACTIVE),
            productRepository.findTop10ByStatusOrderByWhatsappClicksCountDesc(ProductStatus.ACTIVE),
            productRepository.countByCategory()
        );
    }
}

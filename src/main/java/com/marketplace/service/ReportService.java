package com.marketplace.service;

import com.marketplace.dto.ReportForm;
import com.marketplace.entity.Product;
import com.marketplace.entity.ProductStatus;
import com.marketplace.entity.Report;
import com.marketplace.entity.ReportStatus;
import com.marketplace.entity.ReportType;
import com.marketplace.entity.User;
import com.marketplace.exception.NotFoundException;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ReportRepository;
import com.marketplace.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void reportProduct(Product product, User reporter, ReportForm form) {
        Report report = new Report();
        report.setType(ReportType.PRODUCT);
        report.setProduct(product);
        report.setReportedUser(product.getSeller());
        report.setReporter(reporter);
        report.setReason(form.getReason());
        report.setDetail(form.getDetail());
        product.setReportCount(product.getReportCount() + 1);
        reportRepository.save(report);
    }

    @Transactional
    public void reportUser(Long userId, User reporter, ReportForm form) {
        User reported = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Report report = new Report();
        report.setType(ReportType.USER);
        report.setReportedUser(reported);
        report.setReporter(reporter);
        report.setReason(form.getReason());
        report.setDetail(form.getDetail());
        reportRepository.save(report);
    }

    @Transactional
    public void approve(Long id, User admin, String comment, boolean blockUser) {
        Report report = get(id);
        report.setStatus(ReportStatus.APPROVED);
        report.setAdminComment(comment);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        if (report.getType() == ReportType.PRODUCT && report.getProduct() != null) {
            report.getProduct().setStatus(ProductStatus.HIDDEN);
        }
        if (blockUser && report.getReportedUser() != null) {
            report.getReportedUser().setBlocked(true);
        }
    }

    @Transactional
    public void reject(Long id, User admin, String comment) {
        Report report = get(id);
        report.setStatus(ReportStatus.REJECTED);
        report.setAdminComment(comment);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
    }

    public Report get(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new NotFoundException("Reporte no encontrado"));
    }
}

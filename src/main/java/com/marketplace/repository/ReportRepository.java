package com.marketplace.repository;

import com.marketplace.entity.Report;
import com.marketplace.entity.ReportStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @EntityGraph(attributePaths = {"product", "reportedUser", "reporter"})
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    long countByStatus(ReportStatus status);
}

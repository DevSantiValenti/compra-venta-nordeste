package com.marketplace.repository;

import com.marketplace.entity.SiteVisitEvent;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteVisitEventRepository extends JpaRepository<SiteVisitEvent, Long> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

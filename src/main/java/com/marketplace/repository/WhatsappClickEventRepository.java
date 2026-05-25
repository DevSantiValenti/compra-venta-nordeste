package com.marketplace.repository;

import com.marketplace.entity.WhatsappClickEvent;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsappClickEventRepository extends JpaRepository<WhatsappClickEvent, Long> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

package com.marketplace.repository;

import com.marketplace.entity.ProductViewEvent;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductViewEventRepository extends JpaRepository<ProductViewEvent, Long> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

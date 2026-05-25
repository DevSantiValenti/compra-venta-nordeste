package com.marketplace.repository;

import com.marketplace.entity.FeaturedPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeaturedPaymentRepository extends JpaRepository<FeaturedPayment, Long> {
}

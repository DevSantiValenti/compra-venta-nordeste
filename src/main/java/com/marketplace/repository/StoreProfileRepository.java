package com.marketplace.repository;

import com.marketplace.entity.StoreProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreProfileRepository extends JpaRepository<StoreProfile, Long> {
    Optional<StoreProfile> findBySlug(String slug);
}

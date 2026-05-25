package com.marketplace.repository;

import com.marketplace.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByBlockedTrue();
}

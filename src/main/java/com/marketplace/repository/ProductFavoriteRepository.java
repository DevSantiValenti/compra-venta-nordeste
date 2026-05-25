package com.marketplace.repository;

import com.marketplace.entity.Product;
import com.marketplace.entity.ProductFavorite;
import com.marketplace.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
    boolean existsByUserAndProduct(User user, Product product);

    Optional<ProductFavorite> findByUserAndProduct(User user, Product product);

    @EntityGraph(attributePaths = {"product", "product.category", "product.seller", "product.images"})
    List<ProductFavorite> findByUserOrderByCreatedAtDesc(User user);
}
